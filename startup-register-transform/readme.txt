注意：
1、此插件仅针对kotlin代码
2、对于单例代码，必须按照特定单例格式写，比如下面这样
class Test1 private constructor(): IApplication {

    /**
     * 必须用此方法实现单例。否则工程会报错
     */
    companion object {
        val instance: Test1 by lazy {
            Test1()
        }
        const val TAG = "Test1"
    }

    override fun attachBaseContext(context: Context) {
        Log.e(TAG,"attachBaseContext")
    }

    override fun onCreate() {
        Log.e(TAG,"onCreate")
    }
}