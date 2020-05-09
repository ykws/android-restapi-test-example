package io.github.ykws.githubclient

import com.google.common.truth.Truth.*
import io.github.ykws.githubclient.data.GitHubRemoteDataSource
import io.github.ykws.githubclient.data.GitHubService
import io.github.ykws.githubclient.data.Repo
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Test
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class GitHubRemoteDataSourceUnitTest {
  private val mockWebServer = MockWebServer()
  lateinit var gitHubRemoteDataSource: GitHubRemoteDataSource

  @Before
  fun setUp() {
    val dispatcher: Dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        return when {
          request == null -> MockResponse().setResponseCode(400)
          request.path == null -> MockResponse().setResponseCode(400)
          request.path!!.matches(Regex("/users/[a-zA-Z0-9]+/repos")) -> {
            MockResponse()
              .setBodyFromResource("users_ykws_repos.json")
              .setResponseCode(200)
          }
          else -> MockResponse().setResponseCode(404)
        }
      }
    }
    mockWebServer.dispatcher = dispatcher
    mockWebServer.start()

    val retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url(""))
      .client(OkHttpClient())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    val gitHubService = retrofit.create(GitHubService::class.java)
    gitHubRemoteDataSource = GitHubRemoteDataSource(gitHubService)
  }

  @Test
  @Throws(Exception::class)
  fun getUserRepositories() {
    gitHubRemoteDataSource.repos("")
      .test()
      .await()
      .assertNotComplete()

    val list: List<Repo> = gitHubRemoteDataSource.repos("ykws")
      .test()
      .await()
      .assertNoErrors()
      .assertComplete()
      .values()[0]
    assertThat(list).isNotEmpty()
    assertThat(list[0].name).isEqualTo("account-security-quickstart-rails")
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  private fun MockResponse.setBodyFromResource(name: String): MockResponse {
    val inputStream = javaClass.classLoader!!.getResourceAsStream(name)
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    bufferedReader.forEachLine { buffer -> stringBuilder.append(buffer) }

    val body = stringBuilder.toString()
    this.setBody(body)
    return this
  }
}
