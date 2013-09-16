package ru.redmadrobot.test;

public class InstargamServe {
    public static final String GET_ACCESS_TOKEN_URL = "https://instagram.com/oauth/authorize/?client_id=86ca38b187274faf9f3b8954085d5fb2&redirect_uri=http://ya.ru&response_type=token";
    public static final String URL = "https://api.instagram.com/v1/";
    public static final String USERS = "users/";
    public static final String USER_MEDIA = "/media/recent/";
    public static final String SEARCH_USERS = "search?q=";
    public static final String ACCESS_TOKEN = "access_token=554794424.86ca38b.b2a48fe66488472bb359897c3294d085";
    public static final String AND = "&";
    public static final String QUESTION_SYMBOL = "?";
    public static final int RESPONSE_OK = 200;

    public static String searchUsersByName(String name){
        return  URL + USERS + SEARCH_USERS + name + AND + ACCESS_TOKEN;
    }

    public static String getUserPics(String userId){
        return URL + USERS + userId + USER_MEDIA + QUESTION_SYMBOL + ACCESS_TOKEN;
    }

}
