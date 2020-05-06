package io.github.ykws.githubclient.data

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("/users/{user}/repos")
    fun repos(@Path("user") user: String): Single<List<Repo>>
}