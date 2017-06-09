package ru.scratty.action.util;

import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.groups.responses.GetMembersResponse;
import com.vk.api.sdk.queries.groups.GroupField;
import ru.scratty.action.common.VkApi;
import ru.scratty.util.GlobalConfig;

import java.util.List;
import java.util.Random;

/**
 * Утилита для работы с группами vk
 * @author scratty
 */

public class VkGroup extends VkApi {

    private static volatile VkGroup instance;

    private VkGroup(UserActor userActor) {
        super(userActor);
    }

    public static synchronized VkGroup getInstance(UserActor userActor) {
        VkGroup vkGroup = instance;
        if (vkGroup == null) {
            synchronized (VkGroup.class) {
                vkGroup = instance;
                if (vkGroup == null) {
                    instance = vkGroup = new VkGroup(userActor);
                }
            }
        }
        return vkGroup;
    }

    /**
     * Получение id рандомного участника группы
     */
    public synchronized int getRandomUserFromGroup(String groupId) throws ClientException, ApiException {
        int count = getCountUsersInGroup(groupId);
        if (count > 0) {
            int offset = new Random().nextInt(count);

            GetMembersResponse response = vk.groups()
                    .getMembers(userActor)
                    .groupId(groupId)
                    .count(1)
                    .offset(offset)
                    .execute();

            if (response.getCount() > 0 && response.getItems().get(0) != null) {
                return response.getItems().get(0);
            }
        }
        return INT_ERR;
    }

    /**
     * Получение кол-ва участников группы
     */
    public synchronized int getCountUsersInGroup(String groupId) throws ClientException, ApiException {
        List<GroupFull> list = vk.groups()
                .getById(userActor)
                .groupId(groupId)
                .fields(GroupField.MEMBERS_COUNT)
                .execute();

        if (list.size() > 0 && list.get(0).getMembersCount() != null) {
            return list.get(0).getMembersCount();
        }
        return INT_ERR;
    }

    /**
     * Получение рандомной группы из стартового аккаунта из конфига
     */
    public synchronized int getRandomGroupFromUserFromConfig() throws ClientException, ApiException {
        com.vk.api.sdk.objects.groups.responses.GetResponse response = vk.groups()
                .get(userActor)
                .userId(Integer.valueOf(GlobalConfig.getInstance().getField(GlobalConfig.Field.START_USER)))
                .execute();

        if (response.getCount() > 0) {
            return response.getItems().get(new Random().nextInt(response.getCount()));
        }
        return INT_ERR;
    }

    /**
     * Получение рандомной группы от рандомного пользователя
     */
    public synchronized int getRandomGroupFromRandomUser() throws ClientException, ApiException {
        int groupId = getRandomGroupFromUserFromConfig();
        if (groupId != -1) {
            int userId = getRandomUserFromGroup(String.valueOf(groupId));
            if (userId != -1) {
                com.vk.api.sdk.objects.groups.responses.GetResponse response = vk.groups().
                        get(userActor).
                        userId(userId)
                        .execute();

                if (response.getCount() > 0) {
                    return response.getItems().get(new Random().nextInt(response.getCount()));
                }
            }
        }
        return INT_ERR;
    }

    /**
     * Получение имени группы
     */
    public synchronized String getGroupName(int groupId) throws ClientException, ApiException {
        List<GroupFull> list = vk.groups()
                .getById(userActor)
                .groupId(String.valueOf(groupId))
                .execute();

        if (list != null && list.size() > 0) {
            return list.get(0).getName();
        }

        return STRING_ERR;
    }

}