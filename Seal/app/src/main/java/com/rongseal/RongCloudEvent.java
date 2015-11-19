package com.rongseal;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.rongseal.activity.PhotoActivity;
import com.rongseal.activity.ValidationMessageActivity;
import com.rongseal.db.Friend;
import com.rongseal.db.com.rongseal.database.DBManager;
import com.rongseal.message.AgreedFriendRequestMessage;
import com.rongseal.widget.picture.PhotoInputProvider;
import com.sd.core.common.broadcast.BroadcastManager;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imkit.widget.provider.VoIPInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.ImageMessage;

/**
 * Created by AMing on 15/11/6.
 * Company RongCloud
 */
public class RongCloudEvent implements RongIM.ConversationBehaviorListener, RongIMClient.OnReceiveMessageListener, RongIM.ConversationListBehaviorListener {

    public static final java.lang.String FRIEND_MESSAGE = "FRIEND_MESSAGE";
    private static RongCloudEvent mRongCloudInstance;

    private Context mContext;

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (RongCloudEvent.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new RongCloudEvent(context);
                }
            }
        }

    }


    public RongCloudEvent(Context mContext) {
        this.mContext = mContext;
        //初始化不需要 connect 就能 监听的 Listener
        initListener();

    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
//        de.greenrobot.event.EventBus.getDefault().register(this);
        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
    }

    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setConnectedListener() {
        Log.e("test", "test3");
        RongIM.getInstance().getRongIMClient().setOnReceiveMessageListener(this);

        //        扩展功能自定义  singleProvider 语音 voip 只支持单对单
        InputProvider.ExtendProvider[] singleProvider = {
                new PhotoInputProvider(RongContext.getInstance()),//图片
                new CameraInputProvider(RongContext.getInstance()),//相机
                new LocationInputProvider(RongContext.getInstance()),//地理位置
                new VoIPInputProvider(RongContext.getInstance()),// 语音通话
        };

        InputProvider.ExtendProvider[] muiltiProvider = {
                new PhotoInputProvider(RongContext.getInstance()),//图片
                new CameraInputProvider(RongContext.getInstance()),//相机
                new LocationInputProvider(RongContext.getInstance()),//地理位置
        };

        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, muiltiProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.GROUP, muiltiProvider);
    }


    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static RongCloudEvent getInstance() {
        return mRongCloudInstance;
    }

    /**
     * 点击头像的监听
     *
     * @param context
     * @param conversationType
     * @param userInfo
     * @return
     */
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    /**
     * 会话界面长按头像的监听
     *
     * @param context
     * @param conversationType
     * @param userInfo
     * @return
     */
    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    /**
     * 会话界面点击消息的监听
     *
     * @param context
     * @param view
     * @param message
     * @return
     */
    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        if (message.getContent() instanceof ImageMessage) { //实现会话界面点击查看大图逻辑  依赖 PhotoActivity 和 其布局 以及 menu/de_fix_username.xml
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("photo", imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri());
            if (imageMessage.getThumUri() != null)
                intent.putExtra("thumbnail", imageMessage.getThumUri());
            context.startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    /**
     * 会话界面长按消息的监听
     *
     * @param context
     * @param view
     * @param message
     * @return false 走融云默认监听
     */
    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactContentMessage = (ContactNotificationMessage) messageContent;
            BroadcastManager.getInstance(mContext).sendBroadcast(FRIEND_MESSAGE, contactContentMessage);
            Log.e("BroadcastManager", "BroadcastManager");
        }else if(messageContent instanceof AgreedFriendRequestMessage){
            AgreedFriendRequestMessage afrm = (AgreedFriendRequestMessage)messageContent;
            UserInfo userInfo = afrm.getUserInfo();
            if (userInfo != null) {
                DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(new Friend(userInfo.getUserId(),userInfo.getName(),userInfo.getPortraitUri().toString()));
            }
        }
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent messageContent = uiConversation.getMessageContent();
        if (messageContent instanceof ContactNotificationMessage) {
            context.startActivity(new Intent(context, ValidationMessageActivity.class));
            return true;
        }
        return false;
    }
}
