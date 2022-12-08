package com.linkall.vance.example.source;

import com.linkall.vance.config.SourceConfig;

public class ExampleConfig extends SourceConfig {
    private int num;
    private String source;
    private Secret secret;

    @Override
    public Class secretClass() {
        return Secret.class;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Secret getSecret() {
        return secret;
    }

    public void setSecret(Secret secret) {
        this.secret = secret;
    }

    static class Secret {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
