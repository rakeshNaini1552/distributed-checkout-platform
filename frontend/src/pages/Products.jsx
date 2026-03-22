import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import '../App.css';

function Products() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [ordering, setOrdering] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/products')
      .then(res => {
        setProducts(res.data);
        setLoading(false);
      })
      .catch(() => {
        setError('Failed to load products');
        setLoading(false);
      });
  }, []);

  const handleOrder = async () => {
    if (!selectedProduct) return;
    setOrdering(true);
    setMessage('');
    setError('');
    try {
      await api.post('/orders/create', { productId: selectedProduct.id });
      setMessage(`Order placed for ${selectedProduct.name}! Check My Orders for status.`);
      setSelectedProduct(null);
    } catch (err) {
      setError('Failed to place order. Please try again.');
    } finally {
      setOrdering(false);
    }
  };

  const categories = [...new Set(products.map(p => p.category))];

  if (loading) return <div className="center">Loading products...</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <h2 className="page-title">Product Catalog</h2>
        <button onClick={() => navigate('/orders')} className="btn-secondary">
          My Orders
        </button>
      </div>

      {message && <div className="alert-success">{message}</div>}
      {error && <div className="alert-error">{error}</div>}

      {categories.map(category => (
        <div key={category} className="category-section">
          <h3 className="category-title">{category}</h3>
          <div className="product-grid">
            {products
              .filter(p => p.category === category)
              .map(product => (
                <div
                  key={product.id}
                  className={`product-card ${selectedProduct?.id === product.id ? 'selected' : ''}`}
                  onClick={() => setSelectedProduct(product)}
                >
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="product-image"
                    onError={(e) => e.target.style.display = 'none'}
                  />
                  <div className="product-body">
                    <h4 className="product-name">{product.name}</h4>
                    <p className="product-description">{product.description}</p>
                    <p className="product-price">${product.price}</p>
                  </div>
                </div>
              ))}
          </div>
        </div>
      ))}

      {selectedProduct && (
        <div className="order-bar">
          <span className="order-bar-text">
            Selected: <strong>{selectedProduct.name}</strong> — ${selectedProduct.price}
          </span>
          <button
            onClick={handleOrder}
            className="btn-order"
            disabled={ordering}
          >
            {ordering ? 'Placing order...' : 'Place Order'}
          </button>
        </div>
      )}
    </div>
  );
}

export default Products;