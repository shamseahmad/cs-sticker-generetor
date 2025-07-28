document.addEventListener('DOMContentLoaded', function() {
    const nameForm = document.getElementById('nameForm');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const resultsSection = document.getElementById('resultsSection');
    const combinationsContainer = document.getElementById('combinationsContainer');
    const targetNameSpan = document.getElementById('targetName');
    const submitButton = nameForm.querySelector('.primary-button');
    const buttonText = submitButton.querySelector('.button-text');
    const buttonLoader = submitButton.querySelector('.button-loader');

    nameForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(nameForm);
        const name = formData.get('name').trim().toLowerCase();
        const sortOrder = formData.get('sortOrder');

        if (!name) {
            showNotification('Please enter a valid name', 'error');
            return;
        }

        // Show loading states
        showLoadingState();

        try {
            const response = await fetch('/api/stickers/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: name,
                    sortOrder: sortOrder
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const combinations = await response.json();
            console.log('API Response:', combinations);
            
            // Add a small delay for better UX
            setTimeout(() => {
                displayResults(combinations, name);
                hideLoadingState();
            }, 500);

        } catch (error) {
            console.error('Error fetching combinations:', error);
            showNotification('Error generating combinations. Please try again.', 'error');
            hideLoadingState();
        }
    });

    function showLoadingState() {
        // Button loading animation
        submitButton.disabled = true;
        buttonText.style.opacity = '0';
        buttonLoader.style.display = 'flex';
        
        // Show loading spinner
        loadingSpinner.style.display = 'block';
        resultsSection.style.display = 'none';
        combinationsContainer.innerHTML = '';
        
        // Smooth scroll to loading
        loadingSpinner.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function hideLoadingState() {
        // Reset button
        submitButton.disabled = false;
        buttonText.style.opacity = '1';
        buttonLoader.style.display = 'none';
        
        // Hide loading spinner
        loadingSpinner.style.display = 'none';
    }

    function displayResults(combinations, name) {
        targetNameSpan.textContent = name.toUpperCase();
        
        if (!combinations || combinations.length === 0) {
            combinationsContainer.innerHTML = `
                <div class="no-results">
                    <h4 class="no-results-title">No combinations found</h4>
                    <p class="no-results-text">
                        Sorry, we couldn't generate any sticker combinations for "${name}". 
                        Try a different name or check if the stickers database has matching entries.
                    </p>
                </div>
            `;
            resultsSection.style.display = 'block';
            resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            return;
        }

        const combinationsHtml = combinations.map((combo, index) => {
            console.log('Processing combination:', combo);
            
            if (!combo.stickers || combo.stickers.length === 0) {
                return `
                    <div class="sticker-combo">
                        <div class="combo-header">
                            <h4 class="combo-title">Combination ${index + 1}</h4>
                            <span class="combo-price">$0.00</span>
                        </div>
                        <p class="no-results-text">No stickers found for this combination.</p>
                    </div>
                `;
            }

            const isSingleSticker = combo.stickers.length === 1;
            const singleStickerClass = isSingleSticker ? 'single-sticker' : '';

            const stickersHtml = combo.stickers.map((sticker, stickerIndex) => {
                console.log('Processing sticker:', sticker);
                
                const price = combo.prices && combo.prices[stickerIndex] ? combo.prices[stickerIndex] : null;
                const priceValue = price?.price || 0;
                const priceClass = getPriceClass(priceValue);
                
                // Extract sticker name using camelCase field names
                const stickerName = sticker.extractedName || 'Unknown Sticker';
                const fullStickerName = sticker.fullName || stickerName;
                
                // Construct Steam Market URL using search format
                const steamMarketUrl = `https://steamcommunity.com/market/search?appid=730&q=${encodeURIComponent(fullStickerName)}`;
                
                return `
                    <div class="sticker-item ${priceClass}">
                        <div class="sticker-info">
                            <div class="sticker-name-container">
                                <h5 class="sticker-name">${escapeHtml(stickerName)}</h5>
                                <p class="sticker-full-name">${escapeHtml(fullStickerName)}</p>
                            </div>
                            <div class="sticker-actions">
                                <div class="sticker-price-display">
                                    <span class="price-label">Price</span>
                                    <span class="sticker-price">${price ? `$${priceValue.toFixed(2)}` : 'N/A'}</span>
                                </div>
                                <a href="${steamMarketUrl}" target="_blank" rel="noopener noreferrer" class="steam-market-btn">
                                    <svg class="steam-icon" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5h3V8h4v4h3l-5 5z"/>
                                    </svg>
                                    Steam Market
                                    <svg class="external-icon" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19 19H5V5h7V3H5c-1.11 0-2 .89-2 2v14c0 1.11.89 2 2 2h14c1.11 0 2-.89 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"/>
                                    </svg>
                                </a>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');

            const totalPrice = combo.totalPrice || 0;
            const comboTypeText = isSingleSticker ? '🎯 Perfect Match' : `🔄 Combination ${index + 1}`;

            return `
                <div class="sticker-combo ${singleStickerClass}">
                    <div class="combo-header">
                        <h4 class="combo-title">${comboTypeText}</h4>
                        <div class="combo-price-container">
                            <span class="price-label">Total Cost</span>
                            <span class="combo-price">$${totalPrice.toFixed(2)}</span>
                        </div>
                    </div>
                    <div class="stickers-list">
                        ${stickersHtml}
                    </div>
                    ${isSingleSticker ? '<div class="perfect-match-badge">✨ Exact Match Found</div>' : ''}
                </div>
            `;
        }).join('');

        combinationsContainer.innerHTML = combinationsHtml;
        resultsSection.style.display = 'block';
        
        // Add animation delay for each card
        const cards = combinationsContainer.querySelectorAll('.sticker-combo');
        cards.forEach((card, index) => {
            card.style.animationDelay = `${index * 0.1}s`;
        });
        
        // Smooth scroll to results
        setTimeout(() => {
            resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 100);
    }

    function getPriceClass(price) {
        if (price > 5) return 'price-high';
        if (price > 1) return 'price-medium';
        return 'price-low';
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function showNotification(message, type = 'info') {
        // Create a simple notification
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'error' ? 'var(--color-danger)' : 'var(--color-primary)'};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: var(--radius-md);
            box-shadow: var(--shadow-lg);
            z-index: 1000;
            animation: slideInRight 0.3s ease;
            max-width: 300px;
            font-weight: var(--font-weight-medium);
        `;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        // Remove after 3 seconds
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }

    // Add notification animations to CSS
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
        
        .sticker-full-name {
            font-size: 0.875rem;
            color: var(--color-gray-600);
            margin-top: 0.25rem;
        }
    `;
    document.head.appendChild(style);

    // Add smooth input interactions
    const textInput = document.getElementById('playerName');
    const selectInput = document.getElementById('sortOrder');

    [textInput, selectInput].forEach(input => {
        if (input) {
            input.addEventListener('focus', function() {
                this.style.transform = 'scale(1.02)';
            });

            input.addEventListener('blur', function() {
                this.style.transform = 'scale(1)';
            });
        }
    });

    // Add enter key support for better UX
    textInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            nameForm.dispatchEvent(new Event('submit'));
        }
    });
});
