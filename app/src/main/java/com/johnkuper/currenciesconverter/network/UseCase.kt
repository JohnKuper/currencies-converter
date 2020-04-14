package com.johnkuper.currenciesconverter.network

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class UseCase<in P, R> {

    // TODO Kuper handle errors
    operator fun invoke(parameters: P, resultLiveData: MutableLiveData<ResponseResult<R>>): Disposable {
        return execute(parameters)
            .doOnNext { resultLiveData.postValue(ResponseResult.Success(it)) }
            .doOnError { resultLiveData.postValue(ResponseResult.Error(it)) }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    abstract fun execute(params: P): Observable<R>
}