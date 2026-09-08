package com.example.scamshield.demo

import com.example.scamshield.model.AnalysisInput

object DemoScenarios {

    data class DemoItem(
        val title: String,
        val description: String,
        val input: AnalysisInput
    )

    val list = listOf(
        DemoItem(
            title = "Safe URL Check",
            description = "Official search engine domain with standard SSL certificate.",
            input = AnalysisInput.UrlInput("https://www.google.com")
        ),
        DemoItem(
            title = "Suspicious URL Shortener",
            description = "Masked domain pointing to an unverified registration top-level domain.",
            input = AnalysisInput.UrlInput("https://bit.ly/3x8K9m-sec-login.xyz")
        ),
        DemoItem(
            title = "Phishing Bank Spoof",
            description = "Look-alike bank domain requesting immediate login verification.",
            input = AnalysisInput.UrlInput("https://sbi-netbanking-verify.top/update-kyc")
        ),
        DemoItem(
            title = "Valid QR with Fraudulent Context",
            description = "Technically valid UPI QR demanding advance deposit under guise of refund.",
            input = AnalysisInput.QrPayload("upi://pay?pa=refund-agent@upi&pn=VerifiedMerchant&am=2500&tn=Refund+processing+fee+deposit")
        ),
        DemoItem(
            title = "Urgent Account Suspension SMS",
            description = "Coercive SMS threatening police action and immediate disconnection.",
            input = AnalysisInput.TextInput("URGENT: Your bank account will be blocked in 24 hours. Pay ₹5,000 immediately at https://sbi-netbanking-verify.top/update-kyc to prevent legal notice.")
        ),
        DemoItem(
            title = "Prize Reward Screenshot OCR",
            description = "Extracted text from image claiming lottery winnings requiring upfront deposit.",
            input = AnalysisInput.ScreenshotInput("CONGRATULATIONS! You won ₹1,00,000 cash reward. Deposit ₹1,000 processing fee to claim prize immediately.")
        )
    )
}