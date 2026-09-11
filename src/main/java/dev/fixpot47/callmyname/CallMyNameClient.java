package dev.fixpot47.callmyname;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.Locale;

public final class CallMyNameClient implements ClientModInitializer {
    private String lastTriggeredMessage = "";
    private long lastTriggerTimeMs = 0L;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                return;
            }

            String username = client.player.getGameProfile().name();
            if (username == null || username.isBlank()) {
                return;
            }

            // Some LAN/modded chat paths provide no sender profile at all.
            // Only filter our own message when a sender profile is actually available.
            if (sender != null && sender.name() != null && sender.name().equalsIgnoreCase(username)) {
                return;
            }

            checkAndPlay(client, message.getString(), username);
        });

        // Some servers/mods route normal-looking chat through GAME instead of CHAT.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                return;
            }

            String username = client.player.getGameProfile().name();
            if (username == null || username.isBlank()) {
                return;
            }

            String text = message.getString();
            if (looksLikeOwnMessage(text, username)) {
                return;
            }

            checkAndPlay(client, text, username);
        });
    }

    private void checkAndPlay(Minecraft client, String message, String username) {
        if (!containsUsername(message, username)) {
            return;
        }

        // Avoid a double pling if the same message is exposed through both chat event paths.
        long now = System.currentTimeMillis();
        if (message.equals(lastTriggeredMessage) && now - lastTriggerTimeMs < 750L) {
            return;
        }

        lastTriggeredMessage = message;
        lastTriggerTimeMs = now;

        client.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F)
        );
    }

    private static boolean looksLikeOwnMessage(String message, String username) {
        String text = message.strip().toLowerCase(Locale.ROOT);
        String name = username.toLowerCase(Locale.ROOT);

        return text.startsWith("<" + name + ">")
                || text.startsWith(name + ":")
                || text.startsWith("[" + name + "]");
    }

    private static boolean containsUsername(String message, String username) {
        String text = message.toLowerCase(Locale.ROOT);
        String name = username.toLowerCase(Locale.ROOT);

        int fromIndex = 0;
        while (fromIndex <= text.length() - name.length()) {
            int index = text.indexOf(name, fromIndex);
            if (index < 0) {
                return false;
            }

            int end = index + name.length();
            boolean validLeft = index == 0 || !isUsernameCharacter(text.charAt(index - 1));
            boolean validRight = end == text.length() || !isUsernameCharacter(text.charAt(end));

            if (validLeft && validRight) {
                return true;
            }

            fromIndex = index + 1;
        }

        return false;
    }

    private static boolean isUsernameCharacter(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= '0' && character <= '9')
                || character == '_';
    }
}
