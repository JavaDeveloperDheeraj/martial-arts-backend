document.addEventListener('DOMContentLoaded', () => {
    // 1. Elements Selection
    const mobileInput = document.getElementById('mobile');
    const userOtpInput = document.getElementById('userOtp');
    const btnSendOtp = document.getElementById('btnSendOtp');
    const btnVerifyOtp = document.getElementById('btnVerifyOtp');
    const otpTimer = document.getElementById('otpTimer');
    const otpSuccessBadge = document.getElementById('otpSuccessBadge');
    
	// File Input Elements
	    const photoInput = document.getElementById('photo');
	    const signatureInput = document.getElementById('signature');
		
    // Modal Elements (Safe Initialization)
    const modalElement = document.getElementById('otpDialogModal');
    let otpModal = null;
    if (typeof bootstrap !== 'undefined' && modalElement) {
        otpModal = new bootstrap.Modal(modalElement);
    }
    const modalMobileMsg = document.getElementById('modalMobileMsg');
    const modalOtpValue = document.getElementById('modalOtpValue');
    
    // Form & Submit
    const registrationForm = document.getElementById('studentRegistrationForm');
    const btnSubmit = document.getElementById('btnSubmit');
    const alertBox = document.getElementById('alertBox');

    // State Variables
    let isOtpVerified = false;
    let verifiedMobileNumber = "";
    let timerCountdown = null;

	
	
	function resolveFileUrl(fullPath) {
	    if (!fullPath) return 'https://placehold.co/150x150?text=No+File';
	    const normalized = fullPath.replace(/\\/g, '/');
	    const fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
	    return `/files/${encodeURIComponent(fileName)}`;
	}

	function validateUploadedFile(file, expectedType) {
	    if (!file) return { valid: false, message: 'Please select a file.' };
	    const sizeInKB = file.size / 1024;

	    if (expectedType === 'IMAGE') {
	        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
	        if (!allowedTypes.includes(file.type.toLowerCase())) {
	            return { valid: false, message: 'Only JPG, JPEG, or PNG images are allowed.' };
	        }
	        if (sizeInKB < 20 || sizeInKB > 100) {
	            return { 
	                valid: false, 
	                message: `Image size must be between 20 KB and 100 KB. (Selected: ${sizeInKB.toFixed(1)} KB)` 
	            };
	        }
	    } else if (expectedType === 'PDF') {
	        if (file.type.toLowerCase() !== 'application/pdf') {
	            return { valid: false, message: 'Only PDF files are allowed.' };
	        }
	        if (sizeInKB < 50 || sizeInKB > 200) {
	            return { 
	                valid: false, 
	                message: `PDF size must be between 50 KB and 200 KB. (Selected: ${sizeInKB.toFixed(1)} KB)` 
	            };
	        }
	    }
	    return { valid: true, message: 'Valid file' };
	}
	
	// 🎯 REAL-TIME FILE SIZE VALIDATION (Instant Feedback)
	    if (photoInput) {
	        photoInput.addEventListener('change', (e) => {
	            const file = e.target.files[0];
	            if (file) {
	                const check = validateUploadedFile(file, 'IMAGE');
	                if (!check.valid) {
	                    notify(`Photo Error: ${check.message}`, false);
	                    photoInput.value = '';
	                }
	            }
	        });
	    }

	    if (signatureInput) {
	        signatureInput.addEventListener('change', (e) => {
	            const file = e.target.files[0];
	            if (file) {
	                const check = validateUploadedFile(file, 'IMAGE');
	                if (!check.valid) {
	                    notify(`Signature Error: ${check.message}`, false);
	                    signatureInput.value = '';
	                }
	            }
	        });
	    }
    // Helper: Alert Message
    function notify(message, isSuccess = false) {
        if (alertBox) {
            alertBox.className = `alert ${isSuccess ? 'alert-success' : 'alert-danger'} shadow-sm`;
            alertBox.textContent = message;
            alertBox.classList.remove('d-none');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            alert(message);
        }
    }

    // Only numbers allowed
    function restrictToDigits(inputElement) {
        if (!inputElement) return;
        inputElement.addEventListener('input', () => {
            inputElement.value = inputElement.value.replace(/\D/g, '');
        });
    }
    restrictToDigits(mobileInput);
    restrictToDigits(userOtpInput);

    // Timer Logic
    function startTimer(seconds = 60) {
        let remaining = seconds;
        btnSendOtp.disabled = true;

        clearInterval(timerCountdown);
        timerCountdown = setInterval(() => {
            otpTimer.textContent = `Resend OTP in: ${remaining}s`;

            if (--remaining < 0) {
                clearInterval(timerCountdown);
                otpTimer.textContent = '';
                btnSendOtp.disabled = false;
                btnSendOtp.textContent = 'Resend OTP';
            }
        }, 1000);
    }

    // ========================================================
    // 1. SEND OTP
    // ========================================================
    btnSendOtp.addEventListener('click', async () => {
        const mobile = mobileInput.value.trim();

        if (mobile.length !== 10) {
            notify('Please enter a valid 10-digit mobile number.', false);
            mobileInput.focus();
            return;
        }

        btnSendOtp.disabled = true;
        btnSendOtp.textContent = 'Sending...';

        try {
            const response = await fetch(`/api/otp/send?mobile=${encodeURIComponent(mobile)}&role=STUDENT_REGISTRATION`);
            const data = await response.json();

            if (response.ok && data.success) {
                modalMobileMsg.textContent = `OTP has been sent to your mobile: +91 ${mobile}`;
                modalOtpValue.textContent = data.otp;
                
                if (otpModal) {
                    otpModal.show();
                } else {
                    alert(`OTP Sent! Your Demo OTP is: ${data.otp}`);
                }

                userOtpInput.value = '';
                userOtpInput.disabled = false;
                btnVerifyOtp.disabled = false;
                userOtpInput.focus();

                startTimer(60);
            } else {
                notify(data.message || 'Failed to send OTP. Try again.', false);
                btnSendOtp.disabled = false;
                btnSendOtp.textContent = 'Send OTP';
            }
        } catch (error) {
            notify('Server connection failure while requesting OTP.', false);
            btnSendOtp.disabled = false;
            btnSendOtp.textContent = 'Send OTP';
        }
    });

    // ========================================================
    // 2. VERIFY OTP
    // ========================================================
    btnVerifyOtp.addEventListener('click', () => {
        const mobile = mobileInput.value.trim();
        const enteredOtp = userOtpInput.value.trim();

        if (enteredOtp.length !== 6) {
            notify('Please enter a valid 6-digit OTP.', false);
            userOtpInput.focus();
            return;
        }

        if (enteredOtp === modalOtpValue.textContent.trim()) {
            isOtpVerified = true;
            verifiedMobileNumber = mobile;

            mobileInput.readOnly = true;
            mobileInput.classList.add('bg-light');

            userOtpInput.readOnly = true;
            userOtpInput.classList.add('bg-light');
            btnSendOtp.classList.add('d-none');
            btnVerifyOtp.classList.add('d-none');
            clearInterval(timerCountdown);
            otpTimer.textContent = '';

            otpSuccessBadge.classList.remove('d-none');
            notify('Mobile number verified successfully!', true);

        } else {
            notify('Invalid OTP! Please enter the correct code.', false);
            userOtpInput.value = '';
            userOtpInput.focus();
        }
    });

    // ========================================================
    // 3. SUBMIT FORM
    // ========================================================
    registrationForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!isOtpVerified) {
            notify('Security Alert: You must verify your mobile number via OTP before submitting.', false);
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        if (mobileInput.value.trim() !== verifiedMobileNumber) {
            notify('Security Alert: Mobile number mismatch with verified OTP session.', false);
            return;
        }

        const photo = document.getElementById('photo').files[0];
        const signature = document.getElementById('signature').files[0];

        if (!photo || !signature) {
            notify('Please upload both Passport Photo and Signature.', false);
            return;
        }
		
		// 🎯 SUBMISSION PRE-CHECK (Strict 20KB-100KB Boundary)
		        const photoCheck = validateUploadedFile(photo, 'IMAGE');
		        if (!photoCheck.valid) {
		            notify(`Photo: ${photoCheck.message}`, false);
		            return;
		        }

		        const signCheck = validateUploadedFile(signature, 'IMAGE');
		        if (!signCheck.valid) {
		            notify(`Signature: ${signCheck.message}`, false);
		            return;
		        }

        btnSubmit.disabled = true;
        btnSubmit.textContent = 'Processing Admission...';

        const formData = new FormData(registrationForm);

        try {
            const response = await fetch('/api/student/register', {
                method: 'POST',
                body: formData
            });

            const resultText = await response.text();

            if (response.ok) {
                notify(resultText, true);
                registrationForm.reset();

                // स्टेट रीसेट ताकि उसी मोबाइल से या नए सिरे से अगला फॉर्म भी भरा जा सके
                isOtpVerified = false;
                verifiedMobileNumber = "";
                mobileInput.readOnly = false;
                mobileInput.classList.remove('bg-light');
                userOtpInput.readOnly = false;
                userOtpInput.disabled = true;
                userOtpInput.classList.remove('bg-light');
                btnSendOtp.classList.remove('d-none');
                btnSendOtp.disabled = false;
                btnSendOtp.textContent = 'Send OTP';
                btnVerifyOtp.classList.remove('d-none');
                btnVerifyOtp.disabled = true;
                otpSuccessBadge.classList.add('d-none');
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Submit Admission Application';
            } else {
                notify(resultText, false);
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Submit Admission Application';
            }
        } catch (err) {
            notify('Server connection failure during form submission.', false);
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Submit Admission Application';
        }
    });
});