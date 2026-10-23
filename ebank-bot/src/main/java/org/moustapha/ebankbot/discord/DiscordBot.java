//package org.moustapha.ebankbot.discord;
//
//import com.zgamelogic.discord.annotations.DiscordController;
//import com.zgamelogic.discord.annotations.DiscordMapping;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import org.moustapha.ebankbot.agents.EbankAIAgent;
//
//@DiscordController
//public class DiscordBot {
//    private final EbankAIAgent ebankAIAgent;
//
//    public DiscordBot(EbankAIAgent ebankAIAgent) {
//        this.ebankAIAgent = ebankAIAgent;
//    }
//
//    @DiscordMapping
//    private void perform(MessageReceivedEvent event){
//        if (event.getAuthor().isBot()) return;
//
//    }
//}
