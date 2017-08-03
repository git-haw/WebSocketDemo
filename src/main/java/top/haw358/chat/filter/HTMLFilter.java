package top.haw358.chat.filter;

/**
 * HTML工具类
 * @author haw
 * Created by haw on 17-8-3.
 */
public final class HTMLFilter {

    public static String filter(String message) {
        if (message == null)
            return (null);
        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
                case '<':
                    result.append("<");
                    break;
                case '>':
                    result.append(">");
                    break;
                case '&':
                    result.append("&");
                    break;
                case '\"':
                    result.append("\"");
                    break;
                default:
                    result.append(content[i]);
            }
        }
        return (result.toString());
    }
}
