package com.salhe.antibigdata.module

import android.content.Context
import androidx.room.Room
import com.google.gson.*
import com.salhe.antibigdata.data.dao.ProductsDao
import com.salhe.antibigdata.data.database.ProductsDatabase
import com.salhe.antibigdata.service.ProductRemoteService
import com.salhe.antibigdata.utils.SnowFlake
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Provides
    fun provideProductsDatabase(
        @ApplicationContext context: Context
    ): ProductsDatabase {
        return Room.databaseBuilder(
            context,
            ProductsDatabase::class.java,
            "products"
        ).allowMainThreadQueries().build()
    }

    @Provides
    fun provideProductsDao(
        productsDatabase: ProductsDatabase
    ): ProductsDao {
        return productsDatabase.productsDao()
    }

    @Provides
    fun provideSnowFlake() = SnowFlake(1, 1)

    @Provides
    fun provideRetrofit(): Retrofit {
        val typeAdapter = object : JsonSerializer<Date>, JsonDeserializer<Date> {

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").also {
                it.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            }

            override fun serialize(
                src: Date?,
                typeOfSrc: Type?,
                context: JsonSerializationContext?
            ): JsonElement {
                return JsonPrimitive(dateFormat.format(src))
            }

            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): Date {
                return try {
                    dateFormat.parse(json?.getAsString())
                } catch (e: ParseException) {
                    throw JsonParseException(e)
                }
            }
        }
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(Date::class.java, typeAdapter)
            .create()

        return Retrofit.Builder()
            .baseUrl("http://www.salheli.com:2021/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    }

    @Provides
    fun provideProductRemoteService(
        retrofit: Retrofit
    ): ProductRemoteService {
        return retrofit.create(ProductRemoteService::class.java)
    }

}