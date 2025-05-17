import mongoose from "mongoose";
import Order from "../models/order.js";
import Cart from "../models/cart.js";
import Discount from "../models/discount.js";
import DiscountUsage from "../models/discountUsage.js";

import { ORDER_STATUS, RES_MESSAGES } from "../utils/constants.js";
import { isValidOrderStatus, isValidPaymentMethod, printTrace } from "../utils/helper.js"
import { sendOrderEmail } from "../../mail_sender.js";

const TAG = "OrderController";

export const getOrdersByUser = async (req, res) => {
    const userId = req.userId
    try {
        const orders = await Order.find({ user: userId })
            .populate("discount")
            .populate("delivery_address")
            .populate("payment_method")
            .populate({
                path: "items",
                populate: {
                    path: "product",
                    model: "Product",
                    populate: {
                        path: "category",
                        model: "Category",
                    }
                },
            }).lean();
        for (let order of orders) {
            order.discounted_amount = order.amount;
            order.addressTransfer = order.delivery_address;
            order.discountModel = order.discount;
            order.methodPayment = order.payment_method;

            if (order.discount)
                order.discounted_amount = order.amount - (order.amount * order.discount.value) / 100;
        }
        res.status(200).json({
            data: orders,
            isSuccess: true,
            error: "",
        });
    } catch (error) {
        printTrace(TAG, "getOrdersByUser", error);
        res.status(500).json({
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

export const checkDiscountUsage = async (discountId, userId) => {
    try {
        // Check if discount exists and is valid
        const discount = await Discount.findById(discountId);
        if (!discount) {
            return { success: false, error: "Discount not found" };
        }

        // Check if discount is still valid
        const curDate = new Date();
        if (curDate < discount.effective_from || curDate > discount.effective_to || discount.quantity === 0) {
            return { success: false, error: "Discount is no longer valid" };
        }

        // Check if user has already used this discount
        const existingUsage = await DiscountUsage.findOne({
            user_id: userId,
            discount_id: discountId
        });

        if (existingUsage) {
            return { success: false, error: "You have already used this discount" };
        }

        // Create new usage record
        const newUsage = new DiscountUsage({
            user_id: userId,
            discount_id: discountId
        });
        await newUsage.save();

        // Decrease discount quantity
        discount.quantity -= 1;
        await discount.save();

        return { success: true };
    } catch (error) {
        printTrace(TAG, "checkDiscountUsage", error);
        return { success: false, error: "Error processing discount" };
    }
}

export const addNewOrder = async (req, res) => {
    const order = req.body;
    const userId = req.userId
    console.log("order = " + JSON.stringify(order));

    try {
        if (!mongoose.Types.ObjectId.isValid(userId))
            return res.status(404).send(RES_MESSAGES.USER_NOT_EXIST);

        if (order.addressTransfer && !mongoose.Types.ObjectId.isValid(order.addressTransfer._id))
            return res.status(404).send(RES_MESSAGES.DELIVERY_ADDRESS_NOT_EXIST);

        if (order.discountModel && !mongoose.Types.ObjectId.isValid(order.discountModel._id))
            return res.status(404).send(RES_MESSAGES.DISCOUNT_NOT_EXIST);

        if (order.amount <= 0)
            return res.status(400).send(RES_MESSAGES.INVALID_AMOUNT);

        if (!isValidPaymentMethod(order.methodPayment))
            return res.status(400).send(RES_MESSAGES.INVALID_PAYMENT_METHOD);

        // Check discount usage if discount is applied
        if (order.discountModel && order.discountModel._id) {
            const discountCheck = await checkDiscountUsage(order.discountModel._id, order.userId);
            if (!discountCheck.success) {
                return res.status(400).json({
                    isSuccess: false,
                    error: discountCheck.error
                });
            }
        }

        const TIME_STAMP = new Date();
        let newOrder = new Order({
            user: order.userId,
            delivery_address: order.addressTransfer._id,
            discount: order.discountModel._id,
            amount: order.amount,
            payment_method: order.methodPayment,
            items: order.items,
            status: ORDER_STATUS.WAITING,
            created_date: TIME_STAMP,
            modified_date: TIME_STAMP,
        });
        await newOrder.save();

        newOrder = await Order.findOne({ _id: newOrder._id }).populate("discount")
            .populate("user")
            .populate("delivery_address")
            .populate("payment_method")
            .populate({
                path: "items",
                populate: {
                    path: "product",
                    model: "Product",
                    populate: {
                        path: "category",
                        model: "Category",
                    }
                },
            }).lean();

        await Cart.findOneAndUpdate({ user: order.userId }, { items: [] });
        newOrder.discount_value = newOrder.discount ? newOrder.discount.value * newOrder.amount / 100 : 0;
        newOrder.discounted_amount = newOrder.discount ? newOrder.amount - (newOrder.discount.value * newOrder.amount / 100) : newOrder.amount;
        // await sendOrderEmail(newOrder);

        res.status(200).json({
            code: 200,
            error: "",
        });
    } catch (error) {
        printTrace(TAG, "addNewOrder", error);
        res.status(500).send({
            code: 500,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

// Admin
export const getAllOrdersByAdmin = async (req, res) => {
    try {
        const orders = await Order.find()
            .populate("user")
            .populate("discount")
            .populate("delivery_address")
            .populate({
                path: "items",
                populate: {
                    path: "product",
                    model: "Product",
                    populate: {
                        path: "category",
                        model: "Category",
                    }
                },
            }).lean();
        for (let order of orders) {
            order.discounted_amount = order.amount;
            if (order.discount)
                order.discounted_amount = order.amount - (order.amount * order.discount.value) / 100;
        }

        console.log(orders);

        res.status(200).json({
            data: orders,
            isSuccess: true,
            error: "",
        });
    } catch (error) {
        printTrace(TAG, "getAllOrdersByAdmin", error);
        res.status(500).json({
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

export const updateOrderStatus = async (req, res) => {
    const order = req.body;
    console.log(order);
    try {
        if (!mongoose.Types.ObjectId.isValid(order._id))
            return res.status(404).send(RES_MESSAGES.ORDER_NOT_EXIST);

        if (!isValidOrderStatus(order.status))
            return res.status(400).send(RES_MESSAGES.INVALID_ORDER_STATUS);

        const TIMESTAMP = new Date();
        await Order.findOneAndUpdate(
            { _id: order._id },
            { status: order.status, modified_date: TIMESTAMP }
        );

        res.status(200).json({
            isSuccess: true,
            error: "",
            message: RES_MESSAGES.UPDATE_ORDER_STATUS_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "updateOrderStatus", error);
        res.status(500).json({
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

export const cancelOrder = async (req, res) => {
    const order = req.body;
    console.log(order);
    try {
        if (!mongoose.Types.ObjectId.isValid(order._id))
            return res.status(404).send(RES_MESSAGES.ORDER_NOT_EXIST);

        const TIMESTAMP = new Date();
        await Order.findOneAndUpdate(
            { _id: order._id },
            { status: ORDER_STATUS.CANCELED, modified_date: TIMESTAMP }
        );

        res.status(200).json({
            code: 200,
            isSuccess: true,
            error: RES_MESSAGES.CANCEL_ORDER_SUCCESS,
            message: RES_MESSAGES.CANCEL_ORDER_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "cancelOrder", error);
        res.status(500).json({
            code: 500,
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

export const cancelOrderByAdmin = async (req, res) => {
    const order = req.body;
    console.log(order);
    try {
        if (!mongoose.Types.ObjectId.isValid(order._id))
            return res.status(404).send(RES_MESSAGES.ORDER_NOT_EXIST);

        const TIMESTAMP = new Date();
        await Order.findOneAndUpdate(
            { _id: order._id },
            { status: ORDER_STATUS.CANCELED, modified_date: TIMESTAMP }
        );

        res.status(200).json({
            code: 200,
            isSuccess: true,
            error: RES_MESSAGES.CANCEL_ORDER_SUCCESS,
            message: RES_MESSAGES.CANCEL_ORDER_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "cancelOrderByAdmin", error);
        res.status(500).json({
            code: 500,
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}
