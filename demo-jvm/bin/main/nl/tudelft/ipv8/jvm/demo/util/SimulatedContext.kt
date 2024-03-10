package nl.tudelft.ipv8.jvm.demo.util

// Simulate the SharedPreferences interface
interface SharedPreferences {
    fun getString(key: String, defaultValue: String?): String?
    abstract fun edit(): SharedPreferences.Editor
    abstract class Editor {
        abstract fun putString(key: String, value: String?): SharedPreferences.Editor
        abstract fun apply()
    }
}

// Simulate a simple SharedPreferences implementation
class SimpleSharedPreferences : SharedPreferences {
    private val preferences: MutableMap<String, String?> = mutableMapOf()

    override fun getString(key: String, defaultValue: String?): String? {
        return preferences[key] ?: defaultValue
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor()
    }

    inner class Editor : SharedPreferences.Editor() {
        private val editorPreferences: MutableMap<String, String?> = mutableMapOf()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            editorPreferences[key] = value
            return this
        }
        override fun apply() {
            preferences.putAll(editorPreferences)
        }
    }
}

class SimulatedContext {
    fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        if(name=="da" && mode == 0){
            //do something
        }
        return SimpleSharedPreferences()
    }
}

