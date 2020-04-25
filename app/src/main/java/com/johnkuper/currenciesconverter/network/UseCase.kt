package com.johnkuper.currenciesconverter.network

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class UseCase<in P, R> {

    operator fun invoke(parameters: P, resultLiveData: MutableLiveData<ResponseResult<R>>): Disposable {
        return execute(parameters)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { resultLiveData.postValue(ResponseResult.Loading) }
            .subscribe(
                { resultLiveData.postValue(ResponseResult.Success(it)) },
                { resultLiveData.postValue(ResponseResult.Error(it)) }
            )
    }

    abstract fun execute(params: P): Observable<R>
}