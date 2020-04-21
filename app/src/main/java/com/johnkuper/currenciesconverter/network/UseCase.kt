package com.johnkuper.currenciesconverter.network

import androidx.lifecycle.MutableLiveData
import com.johnkuper.currenciesconverter.ui.kuperLog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class UseCase<in P, R> {

    // TODO Kuper handle errors
    operator fun invoke(parameters: P, resultLiveData: MutableLiveData<ResponseResult<R>>): Disposable {
        return execute(parameters)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { resultLiveData.postValue(ResponseResult.Loading) }
            .subscribe({
                resultLiveData.postValue(ResponseResult.Success(it))
                kuperLog("UseCase, onNext()")
            }, {
                resultLiveData.postValue(ResponseResult.Error(it))
                kuperLog("UseCase, onError=$it")
            })
    }

    abstract fun execute(params: P): Observable<R>
}