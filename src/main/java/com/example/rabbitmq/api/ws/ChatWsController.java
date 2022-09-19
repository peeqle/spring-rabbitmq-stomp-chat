package com.example.rabbitmq.api.ws;

import com.example.rabbitmq.api.domains.dto.chat.ChatDTO;
import com.example.rabbitmq.api.domains.dto.MessageDTO;
import com.example.rabbitmq.api.services.ChatService;
import com.example.rabbitmq.api.services.ParticipantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.rabbitmq.api.services.MessageService.generateMessageDto;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWsController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ParticipantService participantService;

    private final ChatService chatService;

    public static final String CREATE_CHAT = "/topic/chat.create";
    public static final String DELETE_CHAT = "/topic/chat.delete";
    public static final String FETCH_CREATE_CHAT_EVENT = "/topic/chat.create.event";
    public static final String FETCH_DELETE_CHAT_EVENT = "/topic/chat.delete.event";
    public static final String SEND_MESSAGE_TO_ALL = "/topic/chat.{chat_id}.messages.all.send";
    public static final String SEND_MESSAGE_TO_PARTICIPANT = "/topic/chat.{chat_id}.messages.participant.{participant_id}.send";
    public static final String FETCH_CHAT_MESSAGES = "/topic/chat.{chat_id}.messages";
    public static final String FETCH_PERSONAL_CHAT_MESSAGES = "/topic/chat.{chat_id}.participants.{participant_id}.messages";

    @MessageMapping(CREATE_CHAT)
    public void createChat(String chatName) {
        chatService.createChat(chatName);
    }

    @MessageMapping(DELETE_CHAT)
    public void deleteChat(String chatId) {
        chatService.deleteChat(chatId);
    }

    @SubscribeMapping(FETCH_CREATE_CHAT_EVENT)
    public ChatDTO fetchCreateChatEvent() {
        return null;
    }

    @SubscribeMapping(FETCH_CREATE_CHAT_EVENT)
    public ChatDTO fetchDeleteChatEvent() {
        return null;
    }

    @SubscribeMapping(FETCH_CHAT_MESSAGES)
    public MessageDTO fetchChatMessage() {
        return null;
    }

    @SubscribeMapping(FETCH_PERSONAL_CHAT_MESSAGES)
    public ResponseEntity<MessageDTO> fetchPersonalChatMessage(
            @DestinationVariable("chat_id") String chatId,
            @DestinationVariable("participant_id") String participantId,
            @Header String simpSessionId) {
        participantService.handleJoinChat(simpSessionId, participantId, chatId);
        return null;
    }

    @MessageMapping(SEND_MESSAGE_TO_ALL)
    public void sendMessageToAll(String messageText, @DestinationVariable("chat_id") String chatId, @Header("simpSessionId") String sessionId) {
        simpMessagingTemplate.convertAndSend(prepareFetchChatMessagesDestinationLink(chatId), generateMessageDto(messageText, sessionId));
    }

    @MessageMapping(SEND_MESSAGE_TO_PARTICIPANT)
    public void sendMessageToParticipant(
            String messageText,
            @DestinationVariable("chat_id") String chatId,
            @DestinationVariable("participant_id") String participantId,
            @Header("simpSessionId") String sessionId) {
        simpMessagingTemplate.convertAndSend(
                prepareFetchPersonalChatMessagesLink(chatId, participantId),
                generateMessageDto(messageText, sessionId)
        );
    }

    private static String prepareFetchChatMessagesDestinationLink(String chatId) {
        return FETCH_CHAT_MESSAGES.replace("{chat_id}", chatId);
    }

    private static String prepareFetchPersonalChatMessagesLink(String chatId, String participantId) {
        return FETCH_PERSONAL_CHAT_MESSAGES.replace("{chat_id}", chatId).replace("{participant_id}", participantId);
    }

    private static String prepareSendMessageToAllLink(String chatId) {
        return SEND_MESSAGE_TO_ALL.replace("{chat_id}", chatId);
    }

    private static String prepareSendMessageToParticipantLink(String chatId, String participantId) {
        return SEND_MESSAGE_TO_PARTICIPANT.replace("{chat_id}", chatId).replace("{participant_id}", participantId);
    }
}