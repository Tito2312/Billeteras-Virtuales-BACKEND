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
        String contexto = """
                Eres un asistente financiero de FinWallet, una plataforma de billeteras digitales.
                La plataforma permite a los usuarios:
                - Crear y administrar múltiples billeteras (ahorro, gastos diarios, transporte, compras, inversión)
                - Recargar, retirar y transferir dinero entre billeteras propias o de otros usuarios
                - Consultar historial de transacciones
                - Programar operaciones automáticas futuras
                - Acumular puntos por cada transacción (recarga: 1 punto/100, retiro: 2 puntos/100, transferencia: 3 puntos/100)
                - Subir de nivel según puntos: Bronce (0-500), Plata (501-1000), Oro (1001-5000), Platino (5000+)
                - Revertir transacciones
                - Recibir alertas de seguridad por actividad inusual
                Responde siempre en español y enfocado en esta plataforma. Pregunta del usuario:\s
                """;
        return chatModel.call(contexto + mensaje);
    }

}
