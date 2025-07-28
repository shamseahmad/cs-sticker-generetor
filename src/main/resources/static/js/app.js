document.addEventListener('DOMContentLoaded', function() {
    const nameForm = document.getElementById('nameForm');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const resultsSection = document.getElementById('resultsSection');
    const combinationsContainer = document.getElementById('combinationsContainer');
    const targetNameSpan = document.getElementById('targetName');

    nameForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(nameForm);
        const name = formData.get('name').trim().toUpperCase();
        const sortOrder = formData.get('sortOrder');

        if (!name) {
            alert('Please enter a valid name');
            return;
        }

        // Show loading, hide results
        loadingSpinner.style.display = 'block';
        resultsSection.style.display = 'none';
        combinationsContainer.innerHTML = '';

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
            displayResults(combinations, name);

        } catch (error) {
            console.error('Error fetching combinations:', error);
            alert('Error generating combinations. Please try again.');
        } finally {
            loadingSpinner.style.display = 'none';
        }
    });

    function displayResults(combinations, name) {
        targetNameSpan.textContent = name;
        
        if (!combinations || combinations.length === 0) {
            combinationsContainer.innerHTML = `
                <div class="alert alert-warning text-center">
                    <h5>No combinations found</h5>
                    <p>Sorry, we couldn't generate any sticker combinations for "${name}". 
                       Try a different name or check if the stickers database has matching entries.</p>
                </div>
            `;
            resultsSection.style.display = 'block';
            return;
        }

        const combinationsHtml = combinations.map((combo, index) => {
            console.log('Processing combination:', combo);
            
            if (!combo.stickers || combo.stickers.length === 0) {
                return `
                    <div class="sticker-combo border rounded p-4 mb-4 shadow-sm">
                        <div class="alert alert-warning">
                            <h5>Combination ${index + 1}</h5>
                            <p>No stickers found for this combination.</p>
                        </div>
                    </div>
                `;
            }

            const stickersHtml = combo.stickers.map((sticker, stickerIndex) => {
                console.log('Processing sticker:', sticker);
                
                const price = combo.prices && combo.prices[stickerIndex] ? combo.prices[stickerIndex] : null;
                const priceClass = getPriceClass(price?.price || 0);
                
                // Extract sticker name using camelCase field names (standard JSON)
                const stickerName = sticker.extractedName || 'Unknown Sticker';
                const fullStickerName = sticker.fullName || stickerName;
                
                // Construct Steam Market URL using search format for better results
                // Always use the current sticker's name to avoid URL swapping issues
                const steamMarketUrl = `https://steamcommunity.com/market/search?appid=730&q=${encodeURIComponent(fullStickerName)}`;
                
                return `
                    <div class="col-md-6 mb-3">
                        <div class="sticker-item ${priceClass} p-3 border rounded">
                            <h6 class="mb-2 text-primary fw-bold">${escapeHtml(stickerName)}</h6>
                            <small class="text-muted d-block mb-2">${escapeHtml(fullStickerName)}</small>
                            ${price ? `
                                <div class="d-flex justify-content-between align-items-center">
                                    <span class="fw-bold text-success">$${price.price.toFixed(2)} ${price.currency}</span>
                                    <a href="${steamMarketUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-primary">
                                        <i class="fas fa-external-link-alt"></i> Steam Market
                                    </a>
                                </div>
                            ` : `
                                <div class="text-muted">
                                    <i class="fas fa-exclamation-triangle"></i> Price unavailable
                                    <br>
                                    <a href="${steamMarketUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-secondary mt-2">
                                        <i class="fas fa-external-link-alt"></i> Search Steam Market
                                    </a>
                                </div>
                            `}
                        </div>
                    </div>
                `;
            }).join('');

            const totalPrice = combo.totalPrice || 0;

            return `
                <div class="sticker-combo border rounded p-4 mb-4 shadow-sm">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="mb-0 text-dark">Combination ${index + 1}</h5>
                        <span class="total-price badge bg-primary fs-6">Total: $${totalPrice.toFixed(2)}</span>
                    </div>
                    <div class="row">
                        ${stickersHtml}
                    </div>
                </div>
            `;
        }).join('');

        combinationsContainer.innerHTML = combinationsHtml;
        resultsSection.style.display = 'block';
        
        // Smooth scroll to results
        resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function getPriceClass(price) {
        if (price > 10) return 'price-high';
        if (price > 1) return 'price-medium';
        return 'price-low';
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});
