package com.talkhub.backend.im.browser;

import com.talkhub.backend.im.session.OnlineUserView;
import com.talkhub.backend.im.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OnlineUserDirectory {

    private final SessionRegistry sessionRegistry;
    private final BrowserSessionBridge browserSessionBridge;

    public OnlineUserDirectory(SessionRegistry sessionRegistry, BrowserSessionBridge browserSessionBridge) {
        this.sessionRegistry = sessionRegistry;
        this.browserSessionBridge = browserSessionBridge;
    }

    public List<OnlineUserView> listOnlineUsers() {
        Map<Long, String> users = new LinkedHashMap<>();
        sessionRegistry.onlineUsers().forEach(user -> users.put(user.userId(), user.username()));
        browserSessionBridge.activeConnections().forEach(connection -> users.put(connection.userId(), connection.username()));
        return users.entrySet().stream()
            .map(entry -> new OnlineUserView(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(OnlineUserView::username, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }
}
