package org.telegram.ui.ProfileActivityV2;

import android.graphics.Color;
import android.os.Bundle;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;

public class ProfileActivityDataProvider {
    public long userId;
    public long chatId;
    public final long dialogId;
    public final long topicId;

    public final boolean isTopic;
    public final boolean isMyProfile;
    public final boolean showAddToContacts;
    public final boolean saved;
    public final boolean reportSpam;

    public TLRPC.EncryptedChat currentEncryptedChat;
    public TL_bots.BotInfo botInfo;

    private final Utilities.Callback0Return<MessagesController> messagesController;
    private final Utilities.Callback0Return<ContactsController> contactsController;
    private final Utilities.Callback0Return<NotificationsController> notificationsController;
    private final Utilities.CallbackReturn<Integer, Integer> colorProvider;

    public ProfileActivityDataProvider(
        Bundle arguments,
        Utilities.Callback0Return<MessagesController> messagesController,
        Utilities.Callback0Return<ContactsController> contactsController,
        Utilities.Callback0Return<NotificationsController> notificationsController,
        Utilities.CallbackReturn<Integer, Integer> colorProvider
    ) {
        userId = arguments.getLong("user_id", 0);
        chatId = arguments.getLong("chat_id", 0);
        dialogId = arguments.getLong("dialog_id", 0);
        topicId = arguments.getLong("topic_id", 0);
        isMyProfile = arguments.getBoolean("my_profile", false);
        showAddToContacts = arguments.getBoolean("show_add_to_contacts", true);
        reportSpam = arguments.getBoolean("reportSpam", false);

        saved = arguments.getBoolean("saved", false);
        isTopic = topicId != 0;

        this.messagesController = messagesController;
        this.contactsController = contactsController;
        this.notificationsController = notificationsController;
        this.colorProvider = colorProvider;

        if (userId != 0 && dialogId != 0) {
            currentEncryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
        }
    }

    public long getDialogId() {
        if (dialogId != 0) {
            return dialogId;
        } else if (userId != 0) {
            return userId;
        } else {
            return -chatId;
        }
    }

    public TLRPC.User getUser() {
        if (userId == 0) return null;
        return messagesController.run().getUser(userId);
    }

    public TLRPC.UserFull getUserFull() {
        if (userId == 0) return null;
        return messagesController.run().getUserFull(userId);
    }

    public TLRPC.Chat getChat() {
        if (chatId == 0) return null;
        return messagesController.run().getChat(chatId);
    }

    public TLRPC.ChatFull getChatFull() {
        if (chatId == 0) return null;
        return messagesController.run().getChatFull(chatId);
    }

    public TLRPC.Dialog getDialog() {
        if (dialogId == 0) return null;
        return messagesController.run().getDialog(dialogId);
    }

    public void setUserFull(TLRPC.UserFull userFull) {
        if (userFull == null) {
            userId = 0;
            return;
        }
        userId = userFull.id;
    }

    public void setChatFull(TLRPC.ChatFull chatFull) {
        if (chatFull == null) {
            chatId = 0;
            return;
        }
        chatId = chatFull.id;
    }

    public MessagesController.PeerColor peerColor() {
        if (getUser() != null) {
            MessagesController.PeerColor peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(getUser().emoji_status);
            if (peerColor == null) {
                final int colorId = UserObject.getProfileColorId(getUser());
                final MessagesController.PeerColors peerColors = messagesController.run().profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }

            return peerColor;
        } else if (getChat() != null) {
            MessagesController.PeerColor peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(getChat().emoji_status);
            if (peerColor == null) {
                final int colorId = ChatObject.getProfileColorId(getChat());
                final MessagesController.PeerColors peerColors = messagesController.run().profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }

            return peerColor;

        }

        return null;
    }

    public boolean needInsetForStories() {
        return messagesController.run().getStoriesController().hasStories(getDialogId()) && !isTopic;
    }

    public boolean isBot() {
        if (getUser() == null) { return false; }
        return getUser().bot;
    }

    public boolean isPremiumUser() {
        if (getUser() == null) return false;
        return messagesController.run().isPremiumUser(getUser());
    }

    public TLRPC.TL_forumTopic findTopic() {
        return messagesController.run().getTopicsController().findTopic(chatId, topicId);
    }

    public boolean isDialogMuted() {
        return messagesController.run().isDialogMuted(-chatId, topicId);
    }

    public boolean premiumFeaturesBlocked() {
        return messagesController.run().premiumFeaturesBlocked();
    }

    public ContactsController getContactsController() {
        return contactsController.run();
    }

    public MessagesController getMessagesController() {
        return messagesController.run();
    }

    public NotificationsController getNotificationsController() {
        return notificationsController.run();
    }

    public TLRPC.EncryptedChat getCurrentEncryptedChat() {
        return messagesController.run().getEncryptedChat(
                DialogObject.getEncryptedChatId(dialogId)
        );
    }

    public int color(int key) {
        return colorProvider.run(key);
    }

    public boolean hasPhoto() {
        if (getUser() != null) {
            return getUser().photo != null && !(getUser().photo instanceof TLRPC.TL_userProfilePhotoEmpty);
        } else if (getChat() != null) {
            return getChat().photo != null && !(getChat().photo instanceof TLRPC.TL_chatPhotoEmpty);
        } else {
            return false;
        }
    }

    public boolean userBlocked() {
        return getMessagesController().blockePeers.indexOfKey(userId) >= 0;
    }

    public boolean hasFallbackPhoto() {
        if (getUserFull() == null) { return false; }
        return UserObject.hasFallbackPhoto(getUserFull());
    }

    public boolean hasPrivacyCommand() {
        if (!isBot()) return false;
        if (getUserFull() == null || getUserFull().bot_info == null) return false;
        if (getUserFull().bot_info.privacy_policy_url != null) return true;
        for (TLRPC.TL_botCommand command : getUserFull().bot_info.commands) {
            if ("privacy".equals(command.command)) {
                return true;
            }
        }
        return true;
    }
}
