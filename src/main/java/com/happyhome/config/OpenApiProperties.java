package com.happyhome.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private final Data data = new Data();
    private final Its its = new Its();
    private final Kakao kakao = new Kakao();
    private final Bus bus = new Bus();
    private final Lh lh = new Lh();
    private final Commercial commercial = new Commercial();

    public Data getData() {
        return data;
    }

    public Its getIts() {
        return its;
    }

    public Kakao getKakao() {
        return kakao;
    }

    public Bus getBus() {
        return bus;
    }

    public Lh getLh() {
        return lh;
    }

    public Commercial getCommercial() {
        return commercial;
    }

    public static class Data {
        private String serviceKey = "";

        public String getServiceKey() {
            return serviceKey;
        }

        public void setServiceKey(String serviceKey) {
            this.serviceKey = serviceKey;
        }
    }

    public static class Its {
        private String apiKey = "";
        private String eventUrl = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getEventUrl() {
            return eventUrl;
        }

        public void setEventUrl(String eventUrl) {
            this.eventUrl = eventUrl;
        }
    }

    public static class Kakao {
        private String javascriptKey = "";
        private String restKey = "";

        public String getJavascriptKey() {
            return javascriptKey;
        }

        public void setJavascriptKey(String javascriptKey) {
            this.javascriptKey = javascriptKey;
        }

        public String getRestKey() {
            return restKey;
        }

        public void setRestKey(String restKey) {
            this.restKey = restKey;
        }
    }

    public static class Bus {
        private String serviceKey = "";
        private String baseUrl = "";

        public String getServiceKey() {
            return serviceKey;
        }

        public void setServiceKey(String serviceKey) {
            this.serviceKey = serviceKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Lh {
        private String noticeUrl = "";
        private String supplyUrl = "";
        private String detailUrl = "";
        private String complexUrl = "";

        public String getNoticeUrl() {
            return noticeUrl;
        }

        public void setNoticeUrl(String noticeUrl) {
            this.noticeUrl = noticeUrl;
        }

        public String getSupplyUrl() {
            return supplyUrl;
        }

        public void setSupplyUrl(String supplyUrl) {
            this.supplyUrl = supplyUrl;
        }

        public String getDetailUrl() {
            return detailUrl;
        }

        public void setDetailUrl(String detailUrl) {
            this.detailUrl = detailUrl;
        }

        public String getComplexUrl() {
            return complexUrl;
        }

        public void setComplexUrl(String complexUrl) {
            this.complexUrl = complexUrl;
        }
    }

    public static class Commercial {
        private String radiusUrl = "";

        public String getRadiusUrl() {
            return radiusUrl;
        }

        public void setRadiusUrl(String radiusUrl) {
            this.radiusUrl = radiusUrl;
        }
    }
}
