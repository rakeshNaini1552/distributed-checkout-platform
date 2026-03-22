import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

function Orders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [recommendations, setRecommendations] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [ordering, setOrdering] = useState(false);
  const [orderMessage, setOrderMessage] = useState('');

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const token = localStorage.getItem('token');
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userId = payload.userId;

      const res = await api.get(`/orders/user/${userId}`);
      const ordersData = res.data;
      setOrders(ordersData);

      const recsMap = {};
      await Promise.all(
        ordersData.map(async (order) => {
          try {
            const recRes = await api.get(`/orders/${order.id}/recommendations`);
            recsMap[order.id] = recRes.data;
          } catch {
            recsMap[order.id] = [];
          }
        })
      );
      setRecommendations(recsMap);
      setLoading(false);
    } catch (err) {
      setError('Failed to load orders');
      setLoading(false);
    }
  };

  const handleRecommendationClick = async (productName) => {
    try {
      const res = await api.get(`/products/name/${productName}`);
      setSelectedProduct(res.data);
      setOrderMessage('');
    } catch {
      alert('Product not found');
    }
  };

  const handleOrderFromModal = async () => {
    if (!selectedProduct) return;
    setOrdering(true);
    try {
      await api.post('/orders/create', { productId: selectedProduct.id });
      setOrderMessage(`Order placed for ${selectedProduct.name}!`);
    } catch {
      setOrderMessage('Failed to place order');
    } finally {
      setOrdering(false);
    }
  };

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) return <div className="center">Loading orders...</div>;
  if (error) return <div className="center alert-error">{error}</div>;

  return (
    <div className="orders-container">
      <div className="page-header">
        <h2 className="page-title">My Orders</h2>
        <button onClick={() => navigate('/products')} className="btn-secondary">
          Browse Products
        </button>
      </div>

      {orders.length === 0 ? (
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>Browse products and place your first order</p>
        </div>
      ) : (
        orders.map(order => (
          <div key={order.id} className="order-card">
            <div className="order-card-header">
              <span className="order-item">{order.item}</span>
              <span className={`status-badge status-${order.status}`}>
                {order.status}
              </span>
            </div>
            <div className="order-meta">
              ${order.price} · Order #{order.id} · {formatDate(order.createdAt)}
            </div>
            {recommendations[order.id]?.length > 0 && (
              <div>
                <p className="recommendations-title">Customers also bought:</p>
                <div className="recommendations-list">
                  {recommendations[order.id].map((rec, index) => (
                    <span
                      key={index}
                      className="recommendation-tag clickable"
                      onClick={() => handleRecommendationClick(rec.productName)}
                    >
                      {rec.productName}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))
      )}

      {/* Product Modal */}
      {selectedProduct && (
        <div className="modal-overlay" onClick={() => setSelectedProduct(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <button
              className="modal-close"
              onClick={() => setSelectedProduct(null)}
            >
              ×
            </button>
            <img
              src={selectedProduct.imageUrl}
              alt={selectedProduct.name}
              className="modal-image"
              onError={(e) => e.target.style.display = 'none'}
            />
            <div className="modal-body">
              <h3 className="modal-title">{selectedProduct.name}</h3>
              <p className="modal-category">{selectedProduct.category}</p>
              <p className="modal-description">{selectedProduct.description}</p>
              <p className="modal-price">${selectedProduct.price}</p>
              {orderMessage ? (
                <div className={orderMessage.includes('Failed')
                  ? 'alert-error' : 'alert-success'}>
                  {orderMessage}
                </div>
              ) : (
                <button
                  className="btn-primary"
                  onClick={handleOrderFromModal}
                  disabled={ordering}
                >
                  {ordering ? 'Placing order...' : 'Place Order'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Orders;