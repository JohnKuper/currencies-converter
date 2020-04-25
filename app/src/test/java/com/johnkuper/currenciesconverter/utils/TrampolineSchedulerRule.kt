package com.johnkuper.currenciesconverter.utils

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TrampolineSchedulerRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                }
            }
        }
    }
}