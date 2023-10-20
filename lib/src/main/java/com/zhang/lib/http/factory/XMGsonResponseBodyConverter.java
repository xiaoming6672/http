package com.zhang.lib.http.factory;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 复刻{{@link retrofit2.converter.gson.GsonResponseBodyConverter}}的Converter
 *
 * @author ZhangXiaoMing 2023-10-18 23:13 周三
 */
public class XMGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;
    private final Type type;

    XMGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter, Type type) {
        this.gson = gson;
        this.adapter = adapter;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(@NonNull ResponseBody value) throws IOException {
        // 如果是String类型直接返回
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz == String.class)
                return (T) value.string();
        }

        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        jsonReader.setLenient(true);
        try {
            T result = adapter.read(jsonReader);
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
            return result;
        } finally {
            value.close();
        }
    }
}
