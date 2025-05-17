import mongoose from "mongoose";
import Discount from "../models/discount.js";
import DiscountUsage from "../models/discountUsage.js";
import { printTrace } from "../utils/helper.js";
import { RES_MESSAGES } from "../utils/constants.js";

const TAG = "DiscountController";

export const addNewDiscount = async (req, res) => {
    const discount = req.body;
    console.log(discount);

    try {
        const TIME_STAMP = new Date();
        const newDiscount = new Discount({
            ...discount,
            effective_from: TIME_STAMP,
            effective_to: new Date().setDate(TIME_STAMP.getDate() + 5),
            created_date: TIME_STAMP,
            modified_date: TIME_STAMP,
        });
        await newDiscount.save();

        res.status(200).json({
            code: 200,
            error: RES_MESSAGES.ADD_DISCOUNT_SUCCESS,
            message: RES_MESSAGES.ADD_DISCOUNT_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "addNewDiscount", error)
        res.status(500).send(RES_MESSAGES.SERVER_ERROR);
    }
}

export const getAllDiscounts = async (req, res) => {
    try {
        const discounts = await Discount.find().lean();
        const curDate = new Date();
        discounts.forEach(item => {
            item.valid = true;
            const effective_from = new Date(item.effective_from);
            const effective_to = new Date(item.effective_to);
            if (curDate < effective_from || curDate > effective_to || item.quantity === 0) {
                item.valid = false;
            }
        })
        console.log(discounts);
        return res.status(200).json({
            data: discounts,
            isSuccess: true,
            error: "",
        });
    } catch (error) {
        printTrace(TAG, "getAllDiscounts", error)
        res.status(500).json({
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}

export const updateDiscount = async (req, res) => {
    const discount = req.body;
    try {
        const TIME_STAMP = new Date();
        await Discount.findOneAndUpdate(
            { _id: discount._id },
            { ...discount, modified_date: TIME_STAMP, },
            { new: true }
        );

        res.status(200).json({
            code: 200,
            error: RES_MESSAGES.UPDATED_DISCOUNT_SUCCESS,
            message: RES_MESSAGES.UPDATED_DISCOUNT_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "updateDiscount", error)
        res.status(500).send(RES_MESSAGES.SERVER_ERROR);
    }
}

export const deleteDiscount = async (req, res) => {
    const { discountId } = req.body;
    console.log("discountId = " + discountId);

    try {
        await Discount.findOneAndDelete({ _id: discountId });

        res.status(200).json({
            code: 200,
            error: RES_MESSAGES.DELETE_DISCOUNT_SUCCESS,
            message: RES_MESSAGES.DELETE_DISCOUNT_SUCCESS,
        });
    } catch (error) {
        printTrace(TAG, "deleteDiscount", error)
        res.status(500).send(RES_MESSAGES.SERVER_ERROR);
    }
}

export const getUserDiscounts = async (req, res) => {
    try {
        const { userId } = req.params;

        if (!userId) {
            return res.status(400).json({
                data: [],
                isSuccess: false,
                error: "User ID is required"
            });
        }

        // Get all discounts
        const discounts = await Discount.find().lean();
        const curDate = new Date();

        // Get all discount usages for this user
        const userDiscountUsages = await DiscountUsage.find({ user_id: userId }).lean();

        // Process each discount
        const processedDiscounts = discounts.map(discount => {
            const effective_from = new Date(discount.effective_from);
            const effective_to = new Date(discount.effective_to);

            // Check if discount is valid
            const isValid = curDate >= effective_from &&
                curDate <= effective_to &&
                discount.quantity > 0;

            // Check if user has used this discount
            const isUsed = userDiscountUsages.some(
                usage => usage.discount_id.toString() === discount._id.toString()
            );

            return {
                ...discount,
                valid: isValid,
                is_used: isUsed
            };
        });

        return res.status(200).json({
            data: processedDiscounts,
            isSuccess: true,
            error: "",
        });
    } catch (error) {
        printTrace(TAG, "getUserDiscounts", error)
        res.status(500).json({
            data: [],
            isSuccess: false,
            error: RES_MESSAGES.SERVER_ERROR,
        });
    }
}