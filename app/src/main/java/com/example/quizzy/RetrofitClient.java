package com.example.quizzy;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class that provides a centralized [Retrofit] instance for network communication.
 * It uses GSON as the default JSON converter and targets the local development server.
 */
public class RetrofitClient {

    /** The base URL for the local backend server (Android Emulator loopback). */
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    
    private static Retrofit retrofit;

    /**
     * Retrieves the singleton instance of Retrofit. 
     * If the instance doesn't exist, it is initialized using a Thread-safe-like 
     * pattern (though not strictly necessary for this application's current usage).
     * 
     * @return The configured [Retrofit] instance.
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
