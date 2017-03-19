package com.viettel.ipcclib.constants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by Macbook on 3/16/17.
 */

public class GsonWrapper {
  private static Gson mGson;

  public static Gson getGson() {
    if (mGson == null) {
      mGson = new GsonBuilder()
          .registerTypeAdapter(MessageType.class, new MessageTypeDeserializer())
          .registerTypeAdapter(MessageType.class, new MessageTypeSerializer())
          .registerTypeAdapter(ConversationId.class, new ConverstationIdDeserializer())
          .registerTypeAdapter(ConversationId.class, new ConversationIdSerializer())
          .create();
    }

    return mGson;
  }

  private static class MessageTypeDeserializer implements
      JsonDeserializer<MessageType> {
    @Override
    public MessageType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
        throws JsonParseException {
      int typeInt = json.getAsInt();

      return MessageType.getValue(typeInt);
    }
  }

  private static class ConverstationIdDeserializer implements
      JsonDeserializer<ConversationId> {
    @Override
    public ConversationId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
        throws JsonParseException {
      try {
        String convId = json.getAsString();

        return ConversationId.valueOf(convId);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
        return null;
      }
    }
  }

  private static class MessageTypeSerializer implements
      JsonSerializer<MessageType> {
    @Override
    public JsonElement serialize(MessageType src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getExtension());
    }
  }

  private static class ConversationIdSerializer implements
      JsonSerializer<ConversationId> {
    @Override
    public JsonElement serialize(ConversationId src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }
  }
}
