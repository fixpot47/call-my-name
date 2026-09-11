package dev.fixpot47.callmyname;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.Locale;

public final class CallMyNameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || sender == null) {
                return;
            }

            String username = client.player.getGameProfile().name();
            String senderName = sender.name();

            if (username == null || username.isBlank()) {
                return;
            }

            if (senderName != null && senderName.equalsIgnoreCase(username)) {
                return;
            }

            if (containsUsername(message.getString(), username)) {
                client.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F)
                );
            }
        });
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
