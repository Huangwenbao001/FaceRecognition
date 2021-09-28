package gjson;

public class LoginData {
    public String code;
    public String msgDetail;
    public String msg;
    public Data data;
    public class Data {
        public String token;

        public String userName;
        public String userId;
        /** 实名认证级别
         * 0：未实名；1：身份证、姓名已匹配；2：身份证、姓名、人脸匹配
         */
        public String level;
    }
}
