let searchTimeout;
let currentFetchController = null;

function blankValue(el){ if (el) el.value = ''; }

function resetForm() {
    const form = document.getElementById('productForm');
    if (!form) return;

    // mark mode
    form.dataset.mode = 'create';
    form.action = '/add-product';
    blankValue(form.querySelector('[name="nameProduct"]'));
    blankValue(form.querySelector('[name="harga"]'));
    blankValue(form.querySelector('[name="des"]'));
    blankValue(form.querySelector('[name="size"]'));
    blankValue(form.querySelector('[name="stok"]'));
    const categorySelect = form.querySelector('select[name="category.id"]');
    if (categorySelect) categorySelect.value = '';

    // Clear file input
    const fileInput = form.querySelector('input[type="file"][name="gambar"]') ||
                      form.querySelector('input[type="file"]');
    if (fileInput) fileInput.value = '';

    // Reset submit button text
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-save me-2"></i>Save Product';
    }

    // Title & preview
    const title = document.getElementById('modal-title-text');
    if (title) title.textContent = 'Add New Product';

    const preview = document.getElementById('image-preview');
    if (preview) {
        preview.style.display = 'none';
        preview.innerHTML = '<img id="preview-img" class="img-thumbnail" style="max-height: 150px;">';
    }

    // Cancel any ongoing fetch for previous edit
    if (currentFetchController) {
        try { currentFetchController.abort(); } catch(_) {}
        currentFetchController = null;
    }

    // Remove validation state
    form.classList.remove('was-validated');
}

function normalizeImageUrl(gambar) {
    if (!gambar) return '';
    // Accept both "filename.jpg" and "/uploads/filename.jpg"
    if (gambar.startsWith('/uploads/')) return gambar;
    return `/uploads/${gambar}`;
}

function populateForm(id) {
    const form = document.getElementById('productForm');
    if (!form) return;

    form.dataset.mode = 'edit';

    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.innerHTML = '<span class="loading"></span> Loading...';
        submitBtn.disabled = true;
    }
    const title = document.getElementById('modal-title-text');
    if (title) title.textContent = 'Edit Product';

    // Abort previous fetch if any
    if (currentFetchController) {
        try { currentFetchController.abort(); } catch(_) {}
    }
    currentFetchController = new AbortController();

    fetch(`/api/product/${id}`, { signal: currentFetchController.signal })
        .then(response => {
            if (!response.ok) throw new Error('Product not found');
            return response.json();
        })
        .then(product => {
            // Fill fields
            const nameInput = form.querySelector('[name="nameProduct"]');
            if (nameInput) nameInput.value = product.nameProduct ?? '';

            const priceInput = form.querySelector('[name="harga"]');
            if (priceInput) priceInput.value = product.harga ?? '';

            const desInput = form.querySelector('[name="des"]');
            if (desInput) desInput.value = product.des ?? '';

            const sizeInput = form.querySelector('[name="size"]');
            if (sizeInput) sizeInput.value = product.size ?? '';

            const stokInput = form.querySelector('[name="stok"]');
            if (stokInput) stokInput.value = product.stok ?? '';

            const categorySelect = form.querySelector('select[name="category.id"]');
            if (categorySelect) {
                const categoryId = product?.category?.id ? String(product.category.id) : '';
                categorySelect.value = categoryId;
            }

            // Preview current image if any
            if (product.gambar) {
                const preview = document.getElementById('image-preview');
                const previewImg = document.getElementById('preview-img') || (function(){
                    const img = document.createElement('img');
                    img.id = 'preview-img';
                    img.className = 'img-thumbnail';
                    img.style.maxHeight = '150px';
                    document.getElementById('image-preview').appendChild(img);
                    return img;
                })();
                previewImg.src = normalizeImageUrl(product.gambar);
                if (preview) preview.style.display = 'block';
            } else {
                const preview = document.getElementById('image-preview');
                if (preview) preview.style.display = 'none';
            }

            // Point form to update endpoint
            form.action = `/update-product/${id}`;

            // Restore submit button
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-save me-2"></i>Update Product';
                submitBtn.disabled = false;
            }
        })
        .catch(err => {
            if (err.name === 'AbortError') return; // ignore
            console.error('Error fetching product:', err);
            // Keep the modal open and show a soft error
            alert('Gagal memuat data produk. Coba lagi.');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-save me-2"></i>Update Product';
                submitBtn.disabled = false;
            }
        });
}

// When modal is fully hidden, always reset form to clean state
document.addEventListener('DOMContentLoaded', function(){
    const modalEl = document.getElementById('productModal');
    if (!modalEl) return;

    modalEl.addEventListener('hidden.bs.modal', () => {
        resetForm();
    });
});