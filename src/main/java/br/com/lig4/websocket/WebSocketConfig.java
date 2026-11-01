package br.com.lig4.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PartidaWebSocketHandler handler;

    public WebSocketConfig(PartidaWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Origens liberadas para o front-end em desenvolvimento; restringir antes de publicar
        registry.addHandler(handler, "/ws/partidas/*").setAllowedOriginPatterns("*");
    }
}
