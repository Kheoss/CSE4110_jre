package java.nl.tudelft.ipv8.jvm.demo.util

// Simulate the SharedPreferences interface
interface SharedPreferences {
    fun getString(key: String, defaultValue: String?): String?
}

// Simulate a simple SharedPreferences implementation
class SimpleSharedPreferences : SharedPreferences {
    private val preferences: MutableMap<String, String?> = mutableMapOf()

    override fun getString(key: String, defaultValue: String?): String? {
        return preferences[key] ?: defaultValue
    }

    fun edit(): SharedPreferences.Editor {
        return Editor()
    }

    inner class Editor : SharedPreferences.Editor {
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

// Then, simulate the Context class with getSharedPreferences method
class SimulatedContext {
    fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return SimpleSharedPreferences()
    }
}

