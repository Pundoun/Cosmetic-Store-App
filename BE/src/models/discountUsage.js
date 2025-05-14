import mongoose from "mongoose";

const DiscountUsageSchema = mongoose.Schema({
    user_id: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    discount_id: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Discount' },
    used_date: { type: Date, default: new Date() }
});

// Create a compound index to ensure a user can only use a discount once
DiscountUsageSchema.index({ user_id: 1, discount_id: 1 }, { unique: true });

export default mongoose.model("DiscountUsage", DiscountUsageSchema); 