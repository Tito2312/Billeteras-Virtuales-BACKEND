package com.fintech.dbilleteras_virtuales.controller;

import org.springframework.ai.chat.model.ChatModel;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class chatGptController {

    private final ChatModel chatModel;

    public chatGptController(ChatModel chatModel) {
        this.chatModel = chatModel;

    }

    @PostMapping("/chatGpt")
    public String chat(@RequestBody String mensaje) {
        String contexto = "Eres un asistente financiero de una plataforma de billeteras digitales. ";
        return chatModel.call(contexto + mensaje);
    }

}
