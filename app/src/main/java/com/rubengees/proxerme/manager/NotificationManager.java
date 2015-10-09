/*
 *   Copyright 2015 Ruben Gees
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.rubengees.proxerme.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import com.proxerme.library.entity.News;
import com.rubengees.proxerme.R;
import com.rubengees.proxerme.activity.DashboardActivity;

import java.util.Arrays;
import java.util.List;

import static android.support.v4.app.NotificationCompat.BigTextStyle;

/**
 * A helper class for displaying notifications.
 *
 * @author Ruben Gees
 */
public class NotificationManager {

    public static final int ELLIPSIS = 0x2026;
    private static final int NEWS_NOTIFICATION_ID = 1423;
    private static final int FITTING_CHARS = 35;

    public static void showNewsNotification(@NonNull Context context, List<News> news, int offset) {
        if (offset > 0 || offset == -2) {
            android.app.NotificationManager notificationManager =
                    (android.app.NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context);

            builder.setAutoCancel(true)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setSmallIcon(R.drawable.ic_stat_proxer);

            if (offset == 1) {
                News current = news.get(0);

                if (current.getSubject().length() > FITTING_CHARS) {
                    builder.setContentText(news.get(0).getSubject().substring(0, FITTING_CHARS));
                } else {
                    builder.setContentText(current.getSubject());
                }

                builder.setStyle(new BigTextStyle(builder).bigText(current.getDescription()));
            } else {
                builder.setContentText(generateNewsNotificationAmount(context, offset))
                        .setStyle(new BigTextStyle(builder)
                                .bigText(generateNewsNotificationBigText(news, offset)));
            }

            builder.setContentIntent(PendingIntent.getActivity(
                    context, 0, DashboardActivity.getSectionIntent(context,
                            DashboardActivity.DRAWER_ID_NEWS), PendingIntent.FLAG_UPDATE_CURRENT));

            notificationManager.notify(NEWS_NOTIFICATION_ID, builder.build());
        }
    }

    private static String generateNewsNotificationAmount(@NonNull Context context, int offset) {
        return offset == NewsManager.OFFSET_TOO_LARGE ?
                context.getString(R.string.notification_amount_more_than_15) :
                (offset + " " + context.getString(R.string.notification_amount_text));
    }

    private static String generateNewsNotificationBigText(List<News> news, int offset) {
        String result = "";

        for (int i = 0; i < offset; i++) {
            if (news.get(i).getSubject().length() >= FITTING_CHARS) {
                result += news.get(i).getSubject().substring(0, FITTING_CHARS) +
                        Arrays.toString(Character.toChars(ELLIPSIS));
            } else {
                result += news.get(i).getSubject();
            }
            result += '\n';
        }

        return result;
    }

}
