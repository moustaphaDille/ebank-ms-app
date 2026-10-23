package org.moustapha.ebankbot.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class EbankAIAgent {
    private final ChatClient chatClient;

    public EbankAIAgent(ChatClient.Builder chatClient,
                        ChatMemory chatMemory, ToolCallbackProvider tools){
        this.chatClient = chatClient
                .defaultSystem("Vous etes un assisstant qui se charge de repondre" +
                        "aux questions en fonction du context fourni." +
                        "si aucun context n'est fourni, repondez je ne sais pas")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(tools)
                .build();
    }

//    public String chat(String query){
//        return chatClient.prompt(query)
//                .call()
//                .content();
//    }
    public String chat(Prompt prompt, String conversationId){
        return chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
}
    public Flux<String> chatStream(Prompt prompt, String conversationId) {
        return chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
