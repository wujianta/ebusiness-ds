package org.newit.microservice.ebusiness.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.newit.microservice.ebusiness.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

@Repository
public class ItemRepository {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;
    //@Cacheable(value = "item",key="'item'+#itemId")
    public Item getItemById(long itemId) {
        redisTemplate.opsForZSet().incrementScore("zsetvalues",itemId,1);
        //redisTemplate.opsForZSet().intersectAndStore("zsetvalues",itemId,1);
        Item item = restTemplate.getForObject("http://localhost:29610/item/" + itemId, Item.class);
        return item;
    }

    public void insert(Item item) {
        restTemplate.postForObject("http://localhost:29610/item/insert", item, JSONObject.class);
    }

    public List<Item> getItemAllList() {
        ResponseEntity<List<Item>>
                responseEntity = restTemplate.exchange("http://localhost:29610/item/allList", HttpMethod.GET, null,
                                                       new ParameterizedTypeReference<List<Item>>() {});
        List<Item> itemList = responseEntity.getBody();
        return itemList;
    }


    public List<Item> getItemAllLists() {
        Set<ZSetOperations.TypedTuple<Object>> tuples =redisTemplate.opsForZSet().reverseRangeWithScores("zsetvalues",0,4);
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = tuples.iterator();
        List<Item> itemList = new ArrayList<Item>();
        while (iterator.hasNext())
        {
            ZSetOperations.TypedTuple<Object> typedTuple = iterator.next();

            Item item = this.getItemById(Long.valueOf(typedTuple.getValue().toString()));

            itemList.add(item);
            System.out.println("value:" + typedTuple.getValue() + "score:" + typedTuple.getScore());
        }
        return itemList;
    }
}
