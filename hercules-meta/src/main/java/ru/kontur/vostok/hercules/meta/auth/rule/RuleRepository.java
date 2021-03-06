package ru.kontur.vostok.hercules.meta.auth.rule;

import ru.kontur.vostok.hercules.curator.CuratorClient;

import java.util.List;

/**
 * @author Gregory Koshelev
 */
public class RuleRepository {
    private final CuratorClient curatorClient;

    public RuleRepository(CuratorClient curatorClient) {
        this.curatorClient = curatorClient;
    }

    public List<String> list() throws Exception {
        List<String> rules = curatorClient.children(zPrefix);
        return rules;
    }

    public void create(String rule) throws Exception {
        curatorClient.createIfAbsent(zPrefix + "/" + rule);
    }

    public void delete(String rule) throws Exception {
        curatorClient.delete(zPrefix + "/" + rule);
    }

    private static String zPrefix = "/hercules/auth/rules";
}
