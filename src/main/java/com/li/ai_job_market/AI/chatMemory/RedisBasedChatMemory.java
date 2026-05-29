package com.li.ai_job_market.AI.chatMemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 基于 Redis 的聊天记忆实现，使用 Kryo 序列化存储对话历史
 */
@Component
public class RedisBasedChatMemory implements ChatMemory {


    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final String keyPrefix;


    // Single constructor for Spring injection. The key prefix is read from configuration
    // property `chat-memory.redis.key-prefix` with a sensible default.
    public RedisBasedChatMemory(RedisTemplate<String, byte[]> redisTemplate,
                                @Value("${chat-memory.redis.key-prefix:chat:memory:}") String keyPrefix) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
        this.keyPrefix = keyPrefix == null || keyPrefix.isEmpty() ? "chat:memory:" : keyPrefix;
    }

    private String keyFor(String conversationId) {
        return keyPrefix + conversationId;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<Message> current = getOrCreateConversation(conversationId);
        current.addAll(messages);
        saveConversation(conversationId, current);
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(keyFor(conversationId));
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        byte[] data = redisTemplate.opsForValue().get(keyFor(conversationId));
        if (data == null || data.length == 0) {
            return new ArrayList<>();
        }
        return deserializeMessages(data);
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        byte[] data = serializeMessages(messages);
        redisTemplate.opsForValue().set(keyFor(conversationId), data);
    }

    private byte[] serializeMessages(List<Message> messages) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); Output output = new Output(baos)) {
            kryo.writeObject(output, messages);
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("序列化消息到 Redis 失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> deserializeMessages(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); Input input = new Input(bais)) {
            Object obj = kryo.readObject(input, ArrayList.class);
            return (List<Message>) obj;
        } catch (Exception e) {
            throw new RuntimeException("从 Redis 反序列化消息失败", e);
        }
    }
}
