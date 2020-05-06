package io.github.ykws.githubclient.data

import android.util.Log
import io.github.ykws.githubclient.data.model.LoggedInUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

  fun login(username: String, password: String): Result<LoggedInUser> {
    try {
      // FIXME: Test
      var retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

      val gitHubService = retrofit.create(GitHubService::class.java)
      val gitHubRemoteDataSource = GitHubRemoteDataSource(gitHubService)
      gitHubRemoteDataSource.repos(username)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          list -> list.forEach { Log.d("retrofit", it.name) }
        }, {
          it.printStackTrace()
        })

      // TODO: handle loggedInUser authentication
      val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
      return Result.Success(fakeUser)
    } catch (e: Throwable) {
      return Result.Error(IOException("Error logging in", e))
    }
  }

  fun logout() {
    // TODO: revoke authentication
  }
}

