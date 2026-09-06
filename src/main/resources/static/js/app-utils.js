function resolveFileUrl(fullPath) {
    if (!fullPath) return 'https://placehold.co/150x150?text=No+File';
    
    const normalized = fullPath.replace(/\\/g, '/');
    
    const fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
    
    return `/files/${encodeURIComponent(fileName)}`;
}

function validateUploadedFile(file, expectedType) {
    if (!file) {
        return { valid: false, message: 'Please select a file.' };
    }

    const sizeInKB = file.size / 1024;

    if (expectedType === 'IMAGE') {
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
        if (!allowedTypes.includes(file.type.toLowerCase())) {
            return { valid: false, message: 'Only JPG, JPEG, or PNG images are allowed.' };
        }
        if (sizeInKB < 20 || sizeInKB > 100) {
            return { 
                valid: false, 
                message: `Image size must be between 20 KB and 100 KB. (Selected size: ${sizeInKB.toFixed(1)} KB)` 
            };
        }
    } else if (expectedType === 'PDF') {
        if (file.type.toLowerCase() !== 'application/pdf') {
            return { valid: false, message: 'Only PDF files are allowed.' };
        }
        if (sizeInKB < 50 || sizeInKB > 200) {
            return { 
                valid: false, 
                message: `PDF size must be between 50 KB and 200 KB. (Selected size: ${sizeInKB.toFixed(1)} KB)` 
            };
        }
    }

    return { valid: true, message: 'Valid file' };
}