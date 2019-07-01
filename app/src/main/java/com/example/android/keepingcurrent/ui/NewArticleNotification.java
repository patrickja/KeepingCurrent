package com.example.android.keepingcurrent.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.model.Article;

import java.util.List;

public class NewArticleNotification {

    private static final String NOTIFICATION_TAG = "NewArticle";
    private static final String NOTIFICATION_CHANNEL_ID = "newArticlesChannelId";
    private static final String NOTIFICATION_CHANNEL_NAME = "Article Updates";
    private static final int NOTIFICATION_ID = 12321;

    private static void createChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            NOTIFICATION_CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notification_channel_description));
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void notify(final Context context, List<Article> newArticles, final int number) {
        createChannel(context);

        final String notificationTitle =
                context.getString(R.string.new_article_notification_title_template);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < newArticles.size(); i++) {
            stringBuilder.append(newArticles.get(i).getTitle()).append("\n");
        }

        String subTitle = stringBuilder.toString();

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.ic_article)
                        .setContentTitle(notificationTitle)
                        .setContentText(subTitle)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setTicker(context.getString(R.string.notification_title))
                        .setNumber(number)
                        .setContentIntent(
                                PendingIntent.getActivity(
                                        context,
                                        0,
                                        new Intent(context, MainActivity.class),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(subTitle)
                                        .setBigContentTitle(notificationTitle)
                                        .setSummaryText(subTitle))
                        .setGroup(NOTIFICATION_TAG)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
    }
}
