package io.github.ykws.githubclient.data

import io.reactivex.Single

class GitHubRemoteDataSource(private val gitHubService: GitHubService) {
    fun repos(user: String): Single<List<Repo>> {
        return gitHubService.repos(user)
    }
}