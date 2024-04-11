//package com.example.denso.local_database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//
//import com.example.denso.dispatch.model.RfidTag
//
//@Database(entities = [RfidTag::class], version = 2, exportSchema = false)
//abstract class LocaleDataBase: RoomDatabase() {
//
//    abstract fun localDataBaseDao(): LocalDataBaseDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: LocaleDataBase? = null
//
//        fun getDatabase(context: Context): LocaleDataBase {
//            val tempInstance = INSTANCE
//            if(tempInstance != null){
//                return tempInstance
//            }
//            synchronized(this){
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    LocaleDataBase::class.java,
//                    "LocaleDataBase")
//                    .fallbackToDestructiveMigration()
//                    .allowMainThreadQueries()
//                    .build()
//                INSTANCE = instance
//                return instance
//            }
//        }
//    }
//
//
//}