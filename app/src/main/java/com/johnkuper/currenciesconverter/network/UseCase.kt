package com.johnkuper.currenciesconverter.network

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

abstract class UseCase<R> {

    // TODO Kuper handle errors
    operator fun invoke(resultLiveData: MutableLiveData<ResponseResult<R>>): Disposable {
        return execute()
            .doOnNext { resultLiveData.postValue(ResponseResult.Success(it)) }
            .doOnError { resultLiveData.postValue(ResponseResult.Error(it)) }
            .subscribe()
    }

    abstract fun execute(): Observable<R>
}