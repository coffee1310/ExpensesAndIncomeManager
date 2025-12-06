package data.initial

import android.app.Application
import data.initial.InitialData

class FinanceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        InitialData.insertInitialData(this)
    }
}