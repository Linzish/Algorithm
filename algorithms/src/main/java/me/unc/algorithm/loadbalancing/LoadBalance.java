package me.unc.algorithm.loadbalancing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 负载均衡算法
 * @Date 2020/4/29 11:37
 * @author LZS
 * @version v1.0
 */
public class LoadBalance {

    /**
     * 测试
     * @param args args
     */
    public static void main(String[] args) {
        LoadBalance loadBalance = new LoadBalance();

        //随机算法
        //loadBalance.loadBalanceTest(new RandomLoadBalanceStrategy(), 10, 1000);

        //加权随机算法
        //loadBalance.loadBalanceTest(new WeightRandomLoadBalanceStrategy(), 10, 1000);

        //轮询算法
        //loadBalance.loadBalanceTest(new PollingLoadBalanceStrategy(), 10, 1000);

        //加权轮询算法
        //loadBalance.loadBalanceTest(new WeightPollingLoadBalanceStrategy(), 10, 1000);

        //最小时延算法
        //loadBalance.testLeastActiveLoadBalance(new LeastActiveLoadBalanceStrategy(), 10);

        //一致性hash算法
        loadBalance.testUniformityHashLoadBalanceStrategy(new UniformityHashLoadBalanceStrategy(), 10);
    }

    /**
     * 服务提供者的实体类,包含了服务的host和port等信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ProviderConfig implements Serializable {
        //通信host
        private String host;
        //通信端口
        private Integer port;
        //请求接口名称
        private String interfaceName;
        //请求方法
        private String[] methods;
        //应用名称
        private String application;
        //权重
        private int weight;
        //调用时间
        private int callTime;
    }

    /**
     * 负载均衡算法父接口
     */
    interface LoadBalanceStrategy {
        //object为扩展参数
        ProviderConfig select(List<ProviderConfig> configs, Object object);
    }

    /**
     * 算法测试
     * @param strategy 负载均衡策略：随机，加权随机，轮询，加权轮询
     * @param configNum 生产者个数
     * @param testCount 测试次数
     */
    public void loadBalanceTest(LoadBalanceStrategy strategy ,int configNum, int testCount ){
        List<ProviderConfig> configs = new ArrayList<>();
        int[] counts = new int[configNum];

        for(int i = 0; i< configNum; i++){
            ProviderConfig config = new ProviderConfig();
            config.setInterfaceName("com.serviceImpl");
            config.setHost("127.0.0.1");
            config.setPort(i);
            config.setWeight(new Random().nextInt(100));
            configs.add(config);
        }

        //System.out.println(configs);

        for(int i = 0; i< testCount ; i++){
            ProviderConfig config = strategy.select(configs,null);
            // System.out.println("选中的:"+config);
            int count = counts[config.getPort()];
            counts[config.getPort()] = ++count;

        }

        for(int i = 0; i< configNum; i++){
            System.out.println("序号:" + i + " 权重：" + configs.get(i).getWeight() + "--次数：" + counts[i]);
        }
    }

    /**
     * 测试最小时延算法
     * @param strategy 最小时延算法
     * @param configNum 生产者个数
     */
    public void testLeastActiveLoadBalance(LoadBalanceStrategy strategy ,int configNum){
        List<ProviderConfig> configs = new ArrayList<>();

        for(int i = 0; i< configNum; i++){
            ProviderConfig config = new ProviderConfig();
            config.setInterfaceName("com.serviceImpl");
            config.setHost("127.0.0.1");
            config.setPort(i);
            config.setWeight(i);
            //这里使用随机数来模拟调用耗时。
            config.setCallTime(new Random().nextInt(100));
            configs.add(config);
        }

        for(ProviderConfig c:configs){
            System.out.println("序号:" + c.getPort()  +"--时延:" + c.getCallTime() );
        }
        System.out.println("--------------");
        ProviderConfig config = strategy.select(configs,null);
        System.out.println("最终选择　序号:" + config.getPort()  +"--时延:" + config.getCallTime() );
    }

    /**
     * 测试哈希一致性算法
     * @param strategy 哈希一致性算法
     * @param configNum 生产者个数
     */
    public void testUniformityHashLoadBalanceStrategy(LoadBalanceStrategy strategy ,int configNum){
        List<ProviderConfig> configs = new ArrayList<>();
        for(int i = 0; i< configNum; i++){
            ProviderConfig config = new ProviderConfig();
            config.setInterfaceName("com.serviceImpl");
            config.setHost("127.0.0.1");
            config.setPort(new Random().nextInt(9999));
            config.setWeight(i);
            config.setCallTime(new Random().nextInt(100));
            configs.add(config);
        }
        ProviderConfig config = strategy.select(configs,"127.0.0.1:1234");
        System.out.println("选择结果:" + config.getHost() + ":" + config.getPort());
    }

    /**
     * 随机算法。
     * 也就是从服务列表中随机选择一个，如果随机数产生算法不好，那么就会导致出现偏向性，
     * 导致有些服务命中概率高，有的服务命中概率低，甚至有的服务命中率为0。最后会导致命中率高的时延很严重。
     * 随机算法的优点是其实现简单。
     */
    static class RandomLoadBalanceStrategy implements LoadBalanceStrategy {
        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            int index = new Random().nextInt(configs.size());
            return configs.get(index);
        }
    }

    /**
     * 加权随机算法
     * 在随机算法的基础上，给每个服务增加一个权重，权重越大，概率越大。
     * 在应用进行分布式部署时，机器硬件性能和环境的差异会导致服务性能出现不一致。
     * 为了解决这个问题，可以给性能差的服务降低权重，给性能好的服务增加权重，以尽可能达到负载均衡的效果。
     */
    static class WeightRandomLoadBalanceStrategy implements LoadBalanceStrategy {
        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            List<ProviderConfig> selectConfigs = new ArrayList<>();
            for (ProviderConfig config : configs) {
                for (int i = 0; i < config.getWeight(); i++) {
                    selectConfigs.add(config);
                }
            }
            int index = new Random().nextInt(selectConfigs.size());
            return selectConfigs.get(index);
        }
    }

    /**
     * 轮询算法
     * 轮询所有的服务，每个服务命中的概率都是一样的。
     * 缺点还是和随机算法一样，还是无法解决机器性能差异的问题。
     */
    static class PollingLoadBalanceStrategy implements LoadBalanceStrategy {
        //使用一个map来缓存每类应用的轮询索引
        private Map<String, Integer> indexMap = new ConcurrentHashMap<>();

        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            //获取本地存储的接口名称轮询索引
            Integer index = indexMap.get(getKey(configs.get(0)));
            if (index == null) {
                //如果为空，存入本地map，返回索引0接口服务信息
                indexMap.put(getKey(configs.get(0)), 0);
                return configs.get(0);
            } else {
                //如果本地已有轮询记录
                index++;
                //判断轮询索引是否超出服务总数，是则归零
                if (index >= configs.size()) {
                    index = 0;
                }
                //本地map记录轮询索引，然后返回当前索引的接口服务信息
                indexMap.put(getKey(configs.get(0)), index);
                return configs.get(index);
            }
        }

        public String getKey(ProviderConfig config) {
            return config.getInterfaceName();
        }
    }

    /**
     * 加权轮询算法
     * 原理和上面的加权随机算法和轮询算法一样
     */
    static class WeightPollingLoadBalanceStrategy implements LoadBalanceStrategy {

        private Map<String, Integer> indexMap = new ConcurrentHashMap<>();

        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            Integer index = indexMap.get(getKey(configs.get(0)));
            if (index == null) {
                indexMap.put(getKey(configs.get(0)), 0);
                return configs.get(0);
            } else {
                List<ProviderConfig> selectConfigs = new ArrayList<>();
                for (ProviderConfig config : configs) {
                    for (int i = 0; i < config.getWeight(); i++) {
                        selectConfigs.add(config);
                    }
                }
                index++;
                if (index >= selectConfigs.size()) {
                    index = 0;
                }
                indexMap.put(getKey(configs.get(0)), index);
                return selectConfigs.get(index);
            }
        }

        public String getKey(ProviderConfig config) {
            return config.getInterfaceName();
        }
    }

    /**
     * 最小时延算法
     * 由于机器性能的差异以及网络传输等原因，会导致集群中不同的应用调用时长不一样。
     * 如果能降低调用耗时长的应用的命中率，提高调用耗时短的命中率，达到动态调整，从而实现最终的负载均衡，那么便可以解决以上性能差异的问题。
     * 缺点是实现起来比较复杂，因为要计算启动之后平均调用耗时。
     */
    static class LeastActiveLoadBalanceStrategy implements LoadBalanceStrategy {
        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            ProviderConfig[] registryConfigs = new ProviderConfig[configs.size()];
            configs.toArray(registryConfigs);
            Arrays.sort(registryConfigs, Comparator.comparingInt(ProviderConfig::getCallTime));
            return registryConfigs[0];
        }
    }

    /**
     * 一致性hash算法
     * 先构造一个长度为232的整数环（这个环被称为一致性Hash环），根据节点名称的Hash值（其分布为[0, 232-1]）将服务器节点放置在这个Hash环上，
     * 然后根据数据的Key值计算得到其Hash值（其分布也为[0, 232-1]），接着在Hash环上顺时针查找距离这个Key值的Hash值最近的服务器节点，完成Key到服务器的映射查找。
     * 一致性hash算法还可以实现一个消费者一直命中一个服务提供者。
     * 缺点是实现相对复杂。同时通过优化hashcode算法和增加虚拟节点解决分布不均的问题。
     */
    static class UniformityHashLoadBalanceStrategy implements LoadBalanceStrategy {
        //虚拟节点
        private static final int VIRTUAL_NODES = 5;

        @Override
        public ProviderConfig select(List<ProviderConfig> configs, Object object) {
            SortedMap<Integer, ProviderConfig> sortedMap = new TreeMap<>();
            //添加虚拟节点
            for (ProviderConfig config : configs) {
                for (int i = 0; i < VIRTUAL_NODES; i++) {
                    sortedMap.put(hash(getKey(config.getHost(), config.getPort(), "&&node" + i)), config);
                }
            }
//            System.out.println(sortedMap);
            int requestHashCode = hash((String) object);
            //截取哈希值从请求哈希值开始到int最大值
            SortedMap<Integer, ProviderConfig> subMap = sortedMap.subMap(requestHashCode, Integer.MAX_VALUE);
            ProviderConfig result;
            if (subMap.size() != 0) {
                //选取最近的一个服务节点
                Integer index = subMap.firstKey();
                result = subMap.get(index);
            } else {
                //没有则随机返回
//                result = sortedMap.get(0);
                result = sortedMap.get(new Random().nextInt(sortedMap.size()));
            }

            //打印测试结果
            new PrintResult(sortedMap, requestHashCode).print();

            return result;
        }

        private String getKey(String host, int port, String node){
            return host + ":" + port + node;
        }

        //此处可使用不同的hash算法
        private int hash(String str) {
            final int p = 16777619;
            int hash = (int)2166136261L;
            for (int i = 0; i < str.length(); i++)
                hash = (hash ^ str.charAt(i)) * p;
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;
            // 如果算出来的值为负数则取其绝对值
            if (hash < 0)
                hash = Math.abs(hash);
            return hash;
        }

        @Data
        static class PrintResult{
            private boolean flag =false;
            private SortedMap<Integer, ProviderConfig> sortedMap;
            private int requestHashCode;

            public PrintResult(SortedMap<Integer, ProviderConfig> sortedMap, int requestHashCode) {
                this.sortedMap = sortedMap;
                this.requestHashCode = requestHashCode;
            }

            public void print(){
                sortedMap.forEach((k,v) -> {
                    if((!flag) && (k > requestHashCode)){
                        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                    System.out.println("hashcode: " + k + "  " + v.getHost() + ":" + v.getPort());
                    if((!flag) && (k > requestHashCode)){
                        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        flag = true;
                    }
                });
                System.out.println("------------------请求的hashcode:" + requestHashCode);
            }
        }
    }

}
