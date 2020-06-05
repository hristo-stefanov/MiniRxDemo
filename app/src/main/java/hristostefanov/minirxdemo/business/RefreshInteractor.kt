package hristostefanov.minirxdemo.business

import io.reactivex.Completable
import javax.inject.Inject

class RefreshInteractor @Inject constructor(private val repository: Repository) {
    fun execute(): Completable {
        return repository.refresh()
    }
}