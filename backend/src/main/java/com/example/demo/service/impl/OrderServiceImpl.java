package com.example.demo.service.impl;

import com.example.demo.dto.CreateOrderItemRequest;
import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.BoughtOrderRowDTO;
import com.example.demo.dto.SoldOrderRowDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Snack;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.SnackMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.OrderService;
import com.example.demo.vo.BoughtOrderItemVO;
import com.example.demo.vo.BoughtOrderVO;
import com.example.demo.vo.CreateOrderVO;
import com.example.demo.vo.SoldRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String STATUS_ON_SALE = "on_sale";
    private static final String STATUS_SOLD_OUT = "sold_out";

    private final UserMapper userMapper;
    private final SnackMapper snackMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(
            UserMapper userMapper,
            SnackMapper snackMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper
    ) {
        this.userMapper = userMapper;
        this.snackMapper = snackMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVO createOrder(CreateOrderRequest request) {
        if (userMapper.selectById(request.getBuyerId()) == null) {
            throw new RuntimeException("buyerId 对应的购买人不存在");
        }

        List<CreateOrderItemRequest> requestItems = request.getItems();
        if (requestItems == null || requestItems.isEmpty()) {
            throw new RuntimeException("items 不能为空");
        }

        Map<Long, Integer> quantityMap = mergeQuantities(requestItems);
        List<PreparedOrderLine> preparedLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantityMap.entrySet()) {
            Snack snack = snackMapper.selectByIdForUpdate(entry.getKey());
            if (snack == null) {
                throw new RuntimeException("商品不存在，snackId=" + entry.getKey());
            }
            if (!STATUS_ON_SALE.equals(snack.getStatus())) {
                throw new RuntimeException("商品状态不是 on_sale，snackId=" + snack.getId());
            }
            if (snack.getStock() < entry.getValue()) {
                throw new RuntimeException("商品库存不足，snackId=" + snack.getId());
            }

            BigDecimal subtotal = snack.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            totalAmount = totalAmount.add(subtotal);
            preparedLines.add(new PreparedOrderLine(snack, entry.getValue(), subtotal));
        }

        LocalDateTime createTime = LocalDateTime.now();
        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setTotalAmount(totalAmount);
        order.setCreateTime(createTime);

        if (orderMapper.insert(order) <= 0 || order.getId() == null) {
            throw new RuntimeException("插入订单失败");
        }

        for (PreparedOrderLine line : preparedLines) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setSnackId(line.snack().getId());
            orderItem.setQuantity(line.quantity());
            orderItem.setPrice(line.snack().getPrice());
            orderItem.setSubtotal(line.subtotal());

            if (orderItemMapper.insert(orderItem) <= 0) {
                throw new RuntimeException("插入订单明细失败，snackId=" + line.snack().getId());
            }
        }

        for (PreparedOrderLine line : preparedLines) {
            int remainingStock = line.snack().getStock() - line.quantity();
            String nextStatus = remainingStock == 0 ? STATUS_SOLD_OUT : STATUS_ON_SALE;

            if (snackMapper.updateStockAndStatus(line.snack().getId(), remainingStock, nextStatus) <= 0) {
                throw new RuntimeException("扣减库存失败，snackId=" + line.snack().getId());
            }
        }

        CreateOrderVO response = new CreateOrderVO();
        response.setOrderId(order.getId());
        response.setTotalAmount(totalAmount);
        response.setCreateTime(createTime);
        return response;
    }

    @Override
    public List<BoughtOrderVO> getBoughtOrders(Long buyerId) {
        List<BoughtOrderRowDTO> rows = orderMapper.selectBoughtOrderRows(buyerId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, BoughtOrderVO> orderMap = new LinkedHashMap<>();
        for (BoughtOrderRowDTO row : rows) {
            BoughtOrderVO order = orderMap.computeIfAbsent(row.getOrderId(), key -> {
                BoughtOrderVO vo = new BoughtOrderVO();
                vo.setId(row.getOrderId());
                vo.setBuyerId(row.getBuyerId());
                vo.setTotalAmount(row.getTotalAmount());
                vo.setCreateTime(row.getCreateTime());
                vo.setItems(new ArrayList<>());
                return vo;
            });

            if (row.getItemId() != null) {
                BoughtOrderItemVO item = new BoughtOrderItemVO();
                item.setId(row.getItemId());
                item.setSnackId(row.getSnackId());
                item.setSnackName(row.getSnackName());
                item.setSnackImage(row.getSnackImage());
                item.setQuantity(row.getQuantity());
                item.setPrice(row.getPrice());
                item.setSubtotal(row.getSubtotal());
                item.setSellerId(row.getSellerId());
                item.setSellerUsername(row.getSellerUsername());
                order.getItems().add(item);
            }
        }

        return new ArrayList<>(orderMap.values());
    }

    @Override
    public List<SoldRecordVO> getSoldRecords(Long sellerId) {
        List<SoldOrderRowDTO> rows = orderItemMapper.selectSoldOrderRows(sellerId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<SoldRecordVO> records = new ArrayList<>(rows.size());
        for (SoldOrderRowDTO row : rows) {
            SoldRecordVO record = new SoldRecordVO();
            record.setOrderId(row.getOrderId());
            record.setBuyerId(row.getBuyerId());
            record.setBuyerUsername(row.getBuyerUsername());
            record.setCreateTime(row.getCreateTime());
            record.setSnackId(row.getSnackId());
            record.setSnackName(row.getSnackName());
            record.setSnackImage(row.getSnackImage());
            record.setQuantity(row.getQuantity());
            record.setPrice(row.getPrice());
            record.setSubtotal(row.getSubtotal());
            records.add(record);
        }

        return records;
    }

    private Map<Long, Integer> mergeQuantities(List<CreateOrderItemRequest> items) {
        Map<Long, Integer> quantityMap = new TreeMap<>();

        for (CreateOrderItemRequest item : items) {
            if (item == null) {
                throw new RuntimeException("商品项不能为空");
            }
            quantityMap.merge(item.getSnackId(), item.getQuantity(), Integer::sum);
        }

        return quantityMap;
    }

    private record PreparedOrderLine(Snack snack, Integer quantity, BigDecimal subtotal) {
    }
}
