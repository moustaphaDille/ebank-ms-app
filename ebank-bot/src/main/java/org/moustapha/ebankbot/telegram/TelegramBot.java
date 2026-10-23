package org.moustapha.ebankbot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.moustapha.ebankbot.agents.EbankAIAgent;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//import javax.annotation.PostConstruct;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.content.Media;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    @Value("${telegram.token}")
    private String telegramBotToken;
    private final EbankAIAgent ebankAIAgent;

    public TelegramBot(EbankAIAgent ebankAIAgent) {
        this.ebankAIAgent = ebankAIAgent;
    }

    @PostConstruct
    public void registerTelegramBot() {
        try {
            log.info("Enregistrement du bot Telegram '{}' en cours...", getBotUsername());
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
            log.info("Bot Telegram '{}' enregistré avec succès (long polling actif).", getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Échec de l'enregistrement du bot Telegram : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }






    @Override
    public void onUpdateReceived(Update telegraRequest) {
        log.info("Update Telegram reçu : {}", telegraRequest);
        try {
            if (!telegraRequest.hasMessage()) {
                log.info("Update sans message (probablement un callback/edit), ignoré.");
                return;
            }

            String messageText = telegraRequest.getMessage().getText();
            Long chatId = telegraRequest.getMessage().getChatId();
            List<PhotoSize> photos = telegraRequest.getMessage().getPhoto();
            List<Media> mediaList = new ArrayList<>();
            String caption = null;

            log.info("chatId={}, messageText='{}', nbPhotos={}", chatId, messageText,
                    photos != null ? photos.size() : 0);

            if (photos != null) {
                caption = telegraRequest.getMessage().getCaption();
                if (caption == null) caption = "What do see in this image";

                for (PhotoSize ps : photos) {
                    String fielId = ps.getFileId();
                    GetFile getFile = new GetFile();
                    getFile.setFileId(fielId);
                    File file = execute(getFile);
                    String filePath = file.getFilePath();
                    String textUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;
                    URL fileUrl = new URL(textUrl);
                    mediaList.add(Media.builder()
                            .id(fielId)
                            .mimeType(MimeTypeUtils.IMAGE_PNG)
                            .data(new UrlResource(fileUrl))
                            .build()
                    );
                    log.info("Photo récupérée : fileId={}, url={}", fielId, textUrl);
                }
            }

            String query = messageText != null ? messageText : caption;
            UserMessage userMessage = UserMessage.builder()
                    .text(query)
                    .media(mediaList)
                    .build();

            log.info("Envoi du 'typing...' à chatId={}", chatId);
            sendTypingQuestion(chatId);

            log.info("Appel de EbankAIAgent avec query='{}'", query);
            //String answer = ebankAIAgent.chat(new Prompt(userMessage));
            String answer = ebankAIAgent.chat(new Prompt(userMessage), String.valueOf(chatId));
            log.info("Réponse reçue de EbankAIAgent : '{}'", answer);

            sendTextMessage(chatId, answer);
            log.info("Réponse envoyée avec succès à chatId={}", chatId);

        } catch (RuntimeException e) {
            log.error("Erreur runtime dans onUpdateReceived : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (TelegramApiException e) {
            log.error("Erreur API Telegram dans onUpdateReceived : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            log.error("URL malformée dans onUpdateReceived : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "Bank96Bot";
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    private void sendTextMessage(long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
        execute(sendMessage);
    }

    private void sendTypingQuestion(long chatId) throws TelegramApiException {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(String.valueOf(chatId));
        sendChatAction.setAction(ActionType.TYPING);
        execute(sendChatAction);
    }
}


//package org.moustapha.ebankbot.telegram;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.moustapha.ebankbot.agents.EbankAIAgent;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.core.io.UrlResource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.MimeTypeUtils;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.api.methods.ActionType;
//import org.telegram.telegrambots.meta.api.methods.GetFile;
//import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.File;
//import org.telegram.telegrambots.meta.api.objects.PhotoSize;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//import javax.annotation.PostConstruct;
//import org.springframework.ai.content.Media;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class TelegramBot  extends TelegramLongPollingBot {
//    @Value("${telegram.token}")
//    private String telegramBotToken;
//    private EbankAIAgent ebankAIAgent;
//
//    public TelegramBot(EbankAIAgent ebankAIAgent) {
//        this.ebankAIAgent = ebankAIAgent;
//    }
//
//    @PostConstruct
//    public  void registerTelegramBot(){
//        try {
//            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
//            api.registerBot(this);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void onUpdateReceived(Update telegraRequest){
//        try {
//            if (!telegraRequest.hasMessage()) return;
//            String messageText = telegraRequest.getMessage().getText();
//            Long chatId = telegraRequest.getMessage().getChatId();
//            List<PhotoSize> photos = telegraRequest.getMessage().getPhoto();
//            List<Media> mediaList = new ArrayList<>();
//            String caption =null;
//
//            if (photos!=null){
//                caption = telegraRequest.getMessage().getCaption();
//                if (caption==null) caption="What do see in this image";
//
//                for (PhotoSize ps : photos){
//                    String fielId = ps.getFileId();
//                    GetFile getFile = new GetFile();
//                    getFile.setFileId(fielId);
//                    File file = execute(getFile);
//                    String filePath = file.getFilePath();
//                    String textUrl = "https://api.telegram.org/file/bot"+getBotToken()+"/"+filePath;
//                    URL fileUrl = new URL(textUrl);
//                    mediaList.add(Media.builder()
//                            .id(fielId)
//                            .mimeType(MimeTypeUtils.IMAGE_PNG)
//                            .data(new UrlResource(fileUrl))
//                            .build()
//                    );
//                }
//            }
//            String query = messageText!=null?messageText:caption;
//            UserMessage userMessage = UserMessage.builder()
//                    .text(query)
//                    .media(mediaList)
//                    .build();
//            sendTypingQuestion(chatId);
//            //String answer = ebankAIAgent.chat(new Prompt(userMessage));
//            String answer = ebankAIAgent.chat(new Prompt(userMessage), String.valueOf(chatId));
//            sendTextMessage(chatId, answer);
//
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//       // } catch (MalformedURLException e) {
//            //throw new RuntimeException(e);
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public String getBotUsername() {
//        return "Bank96Bot";
//    }
//
//    @Override
//    public String getBotToken(){
//        return telegramBotToken;
//    }
//    private void sendTextMessage(long chatId, String text) throws TelegramApiException{
//        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
//        execute(sendMessage);
//    }
//    private void sendTypingQuestion(long chatId) throws TelegramApiException{
//        SendChatAction sendChatAction = new SendChatAction();
//        sendChatAction.setChatId(String.valueOf(chatId));
//        sendChatAction.setAction(ActionType.TYPING);
//        execute(sendChatAction);
//
//    }
//}
